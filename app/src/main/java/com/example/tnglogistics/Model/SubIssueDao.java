package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubIssueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSubIssue(SubIssue subIssue);

    @Query("SELECT * From Sub_Issue WHERE IssueTypeCode = :IssueTypeCode")
    LiveData<List<SubIssue>> getSubIssueByIssueTypeCode(int IssueTypeCode);

    @Query("DELETE FROM Sub_Issue")
    void deleteAll();
}
