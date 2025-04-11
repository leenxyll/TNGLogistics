package com.example.tnglogistics.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEmployee(Employee employee);

    @Query("SELECT * From Employee WHERE EmpCode = :EmpCode")
    LiveData<Employee> getEmployeeByEmpCode(int EmpCode);
}
