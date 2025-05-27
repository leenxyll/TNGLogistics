package com.example.tnglogistics.Model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface IssueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssue(Issue issue);
}
