package com.example.tnglogistics.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "Sub_Issue")
public class SubIssue {
    @PrimaryKey
    private int SubIssueTypeCode;
    private String SubIssueTypeName;
    private String RequirePic;
    private int IssueTypeCode;

    public int getSubIssueTypeCode() {
        return SubIssueTypeCode;
    }

    public void setSubIssueTypeCode(int subIssueTypeCode) {
        SubIssueTypeCode = subIssueTypeCode;
    }

    public String getSubIssueTypeName() {
        return SubIssueTypeName;
    }

    public void setSubIssueTypeName(String subIssueTypeName) {
        SubIssueTypeName = subIssueTypeName;
    }

    public String getRequirePic() {
        return RequirePic;
    }

    public void setRequirePic(String requirePic) {
        RequirePic = requirePic;
    }

    public int getIssueTypeCode() {
        return IssueTypeCode;
    }

    public void setIssueTypeCode(int issueTypeCode) {
        IssueTypeCode = issueTypeCode;
    }
}
