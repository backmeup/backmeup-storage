package org.backmeup.storage.logic.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.fileencryption.EncryptionInputStream;
import org.backmeup.keyserver.fileencryption.EncryptionOutputStream;
import org.backmeup.keyserver.fileencryption.FileKeystore;
import org.backmeup.keyserver.fileencryption.Keystore;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.storage.logic.StorageLogic;
import org.backmeup.storage.model.Metadata;
import org.backmeup.storage.model.StorageUser;
import org.backmeup.storage.model.utils.StringUtils;
import org.backmeup.storage.service.config.Configuration;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@RequestScoped
public @Alternative class EncryptedLocalFilesystemStorage implements StorageLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedLocalFilesystemStorage.class);
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());
    private static final String BASE_PATH = Configuration.getProperty("backmeup.storage.home");
    private static final String DIGEST_ALGORITHM = "MD5";

    @Inject
    private KeyserverClient keyserverClient;

    public EncryptedLocalFilesystemStorage() {

    }

    @Override
    public File getFile(StorageUser user, String path) {
        return getFile(user, user.getUserId().toString(), path);
    }

    @Override
    public File getFile(StorageUser user, String owner, String path) {
        final String userPath = getUserFilePath(path, owner);
        final String completePath = BASE_PATH + userPath;
        final Path filePath = Paths.get(completePath);

        return new File(filePath.toAbsolutePath().toString());
    }

    @Override
    public InputStream getFileAsInputStream(StorageUser user, String owner, String path) {
        File file = getFile(user, owner, path);
        PrivateKey privateKey = null;
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, user.getAuthToken());
            byte[] privKey = this.keyserverClient.getPrivateKey(token);
            privateKey = KeyserverClient.decodePrivateKey(privKey);
            return new EncryptionInputStream(file, user.getUserId().toString(), privateKey);
        } catch (KeyserverException | IOException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Metadata saveFile(StorageUser user, String filePath, boolean overwrite, long contentLength, InputStream content) {
        final String userFilePath = getUserFilePath(filePath, user.getUserId().toString());
        final String completePath = BASE_PATH + userFilePath;
        final Path path = Paths.get(completePath);

        PublicKey publicKey;
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, user.getAuthToken());
            byte[] pubKey = this.keyserverClient.getPublicKey(token);
            publicKey = KeyserverClient.decodePublicKey(pubKey);
        } catch (KeyserverException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        File file = new File(path.toAbsolutePath().toString());
        if (file.exists() && !overwrite) {
            throw new WebApplicationException(Status.CONFLICT);
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        long totalLength = 0;

        File parent = new File(path.getParent().toAbsolutePath().toString());
        if (!parent.mkdirs()) {
            LOGGER.info("Unable to create parent directory " + parent);
            // maybe throw a filenotfoundexception
        }
        ;

        if (!file.canWrite()) {
            file.setWritable(true);
        }

        try (OutputStream out = new EncryptionOutputStream(file, user.getUserId().toString(), publicKey)) {
            InputStream in = content;
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                md.update(buffer, 0, len);
                totalLength += len;
            }
        } catch (IOException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        String hash;
        try {
            hash = StringUtils.getHexString(md.digest());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("", e);
            hash = "-1";
        }
        return new Metadata(totalLength, hash, new Date(), filePath);
    }

    @Override
    public void addFileAccessRights(Long bmuUserIdToAdd, String ksUserIdToAdd, StorageUser currUser, String owner, String filePath) {
        //check if the user that we want to add already has access rights
        boolean bAccess = hasFileAccessRights(bmuUserIdToAdd, owner, filePath);
        if (bAccess) {
            return;
        }
        //add access rights to the user
        File file = getFileFromStorage(filePath, owner);
        try {
            Keystore ks = EncryptionInputStream.getKeystore(file);
            //get the access token for the current user
            TokenDTO token = TokenDTO.fromTokenString(currUser.getAuthToken());
            //get the current user's private key
            PrivateKey currUserPrivKey = this.getStorageUserKSPrivateKey(token);
            //load secret key into keystore = same for all users of this file
            ks.getSecretKey(currUser.getUserId() + "", currUserPrivKey);
            PublicKey userToAddPubKey = this.getPublicKey(ksUserIdToAdd);
            //now add the additional user to the list of receivers
            ks.addReceiver(bmuUserIdToAdd + "", userToAddPubKey);
            //save keystore
            ((FileKeystore) ks).save();
        } catch (Exception e) {
            LOGGER.debug("failed to modify keystore receiver list for user: {} on file: {}", bmuUserIdToAdd, file.getAbsolutePath());
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        //check if access right changes were executed properly
        try {
            boolean b = this.hasFileAccessRights(bmuUserIdToAdd, owner, filePath);
            if (!b) {
                LOGGER.error("failed to add file access for user: {} with ks_userID: {} on file: {}; requested by user/owner: {}",
                        bmuUserIdToAdd, ksUserIdToAdd, file.getAbsolutePath(), currUser.getUserId());
                throw new Exception("failed to add file access");
            }
        } catch (Exception e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public void removeFileAccessRights(Long bmuUserIdToRemove, StorageUser currUser, String owner, String filePath) {
        //check if the user that we want to add already has access rights
        boolean bAccess = hasFileAccessRights(bmuUserIdToRemove, owner, filePath);
        if (!bAccess) {
            return;
        }
        File file = getFileFromStorage(filePath, owner);
        //revoke access rights from the user
        try {
            Keystore ks = EncryptionInputStream.getKeystore(file);
            //get the access token for the current user
            TokenDTO token = TokenDTO.fromTokenString(currUser.getAuthToken());
            //get the current user's private key
            PrivateKey currUserPrivKey = this.getStorageUserKSPrivateKey(token);
            //load secret key into keystore = same for all users of this file
            ks.getSecretKey(currUser.getUserId() + "", currUserPrivKey);
            //now add the additional user to the list of receivers
            ks.removeReceiver(bmuUserIdToRemove + "");
            //save keystore
            ((FileKeystore) ks).save();
        } catch (Exception e) {
            LOGGER.debug("failed to modify keystore receiver list for user: {} on file: {}", bmuUserIdToRemove, file.getAbsolutePath());
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        //check if access right changes were executed properly
        try {
            boolean b = this.hasFileAccessRights(bmuUserIdToRemove, owner, filePath);
            if (b) {
                LOGGER.error("failed to remove file access for user: {} on file: {}; requested by user: {}", bmuUserIdToRemove,
                        file.getAbsolutePath(), currUser.getUserId());
                throw new Exception("failed to remove file access");
            }
        } catch (Exception e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean hasFileAccessRights(Long userIdToCheck, String owner, String filePath) {
        File file = getFileFromStorage(filePath, owner);
        try {
            //check on the user's access rights on this file        
            Keystore ks = EncryptionInputStream.getKeystore(file);
            return ks.hasReceiver(userIdToCheck + "");
        } catch (IOException e) {
            LOGGER.error("", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected String getUserFilePath(String filePath, String userId) {
        return "/" + userId + "/" + filePath;
    }

    protected File getFileFromStorage(String filePath, String owner) {
        final String userFilePath = getUserFilePath(filePath, owner);
        final String completePath = BASE_PATH + userFilePath;
        final Path path = Paths.get(completePath);

        File file = new File(path.toAbsolutePath().toString());
        checkFileExistance(file);
        return file;
    }

    protected void checkFileExistance(File file) {
        if ((!file.exists() || (!file.canRead()))) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected PrivateKey getStorageUserKSPrivateKey(TokenDTO token) {
        try {
            byte[] privKey = this.keyserverClient.getPrivateKey(token);
            return KeyserverClient.decodePrivateKey(privKey);
        } catch (KeyserverException ke) {
            LOGGER.debug("error getting private key for storage user", ke);
            throw new WebApplicationException(ACCESS_DENIED);
        }
    }

    protected PublicKey getPublicKey(String userKeyserverId) {
        try {
            byte[] pubK = this.keyserverClient.getPublicKey(userKeyserverId);
            return KeyserverClient.decodePublicKey(pubK);
        } catch (KeyserverException ke) {
            LOGGER.debug("error getting public key for keyserver user: " + userKeyserverId, ke);
            throw new WebApplicationException(ACCESS_DENIED);
        }
    }
}
