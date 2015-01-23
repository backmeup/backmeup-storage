package org.backmeup.storage.service.auth;

import java.util.Date;

public class AuthInfo {
    private String accessToken;
    private Date issueDate;
    
    public AuthInfo() {
        
    }
    
    public AuthInfo(String accessToken, Date issueDate) {
        super();
        this.accessToken = accessToken;
        this.issueDate = (Date) issueDate.clone();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getIssueDate() {
        return (Date) issueDate.clone();
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = (Date) issueDate.clone();
    }
}

