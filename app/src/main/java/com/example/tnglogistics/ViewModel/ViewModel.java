package com.example.tnglogistics.ViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Model.Employee;
import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.Model.Issue;
import com.example.tnglogistics.Model.MileLog;
import com.example.tnglogistics.Model.Repository;
import com.example.tnglogistics.Model.SubIssue;

import java.util.ArrayList;
import java.util.List;

public class ViewModel extends AndroidViewModel {
    private static ViewModel instance;
    private final Repository repository;
    private final MediatorLiveData<List<Invoice>> invoiceList = new MediatorLiveData<>();
//    private LiveData<List<Invoice>> invoicesGrouped;

    public ViewModel(Application application) {
        super(application);
        repository = new Repository(application);
        invoiceList.addSource(repository.getAllInvoice(), invoiceList::setValue);
//        invoicesGrouped = repository.getInvoicesGroupedByLocation();
    }

    public static ViewModel getInstance(Application application){
        if (instance == null) {
            instance = new ViewModel(application);
        }
        return instance;
    }


    // เพิ่มเมธอดนี้เพื่อรีเซ็ต singleton
    public static void resetInstance() {
        instance.invoiceList.setValue(new ArrayList<>());
        // ล้างข้อมูลในฐานข้อมูลผ่าน repository
        instance.repository.clearAllData();
        instance = null;
    }

    public LiveData<List<SubIssue>> getSubIssueByIssueTypeCode(int IssueTypeCode){
        return repository.getSubIssueByIssueTypeCode(IssueTypeCode);
    }

    public void insertEmployee(Employee employee){
        repository.insertEmployee(employee);
    }

    public LiveData<Employee> getEmployee(int EmpCode){
        return repository.getEmployeeByEmpCode(EmpCode);
    }

    public LiveData<List<Invoice>> getInvoiceList() {
        return invoiceList;
    }

    public LiveData<List<Invoice>> getInvoiceBySeq(){
        return repository.getInvoiceBySeq();
    }
    public Invoice getInvoiceByGeofenceID(String GeofenceID){
        return repository.getInvoiceByGeofenceID(GeofenceID);
    }


    public LiveData<Integer> countInvoicesWithStatusFour(){
        return repository.countInvoicesWithStatusFour();
    }

    public LiveData<Integer> countInvoicesWithStatusFive(){
        return repository.countInvoicesWithStatusFive();
    }

    public void getShipmentList(Context context, final Repository.StatusCallback callback){
        repository.getShipmentList(context, callback);
    }

    public void getSubIssue(){
        repository.getSubIssue();
    }

    public void update(Invoice invoice){
        repository.updateInvoice(invoice);
    }

    public void updateInvoiceStatus(Invoice invoice, int seq, int statusCode, Double lat, Double lng, long timeStamp, Context context){
        repository.updateInvoiceStatus(invoice, seq, statusCode, lat, lng, timeStamp, context);
    }

    public void updateInvoiceStatus(Invoice invoice, int seq, int statusCode, Double lat, Double lng, long timeStamp, String issueDesc, int issueTypeCode, Context context){
        repository.updateInvoiceStatus(invoice, seq, statusCode, lat, lng, timeStamp, issueDesc, issueTypeCode, context);
    }

//    public LiveData<List<Invoice>> getInvoicesGroupedByLocation() {
//        return invoicesGrouped;
//    }

    public int getNextShipListPic(String invoiceCode){
        return repository.getNextShipListPic(invoiceCode);
    }

    public int getNextMileLogSeq(String tripCode){
        return repository.getNextMileLogSeq(tripCode);
    }

    public int getNextInvoiceLogSeq(String invoiceCode){
        return repository.getNextInvoiceLogSeq(invoiceCode);
    }

    public List<MileLog> getUnsyncedMileLogsImage(){
        return repository.getUnsyncedMileLogsImage();
    }

    public void updateMile(Context context,int MileLogSeq, int MileLogRecord, long MileLogUpdate, Double MileLogLat, Double MileLogLong, String MileLogPicPath, int MileLogTypeCode){
        repository.updateMile(context, MileLogSeq, MileLogRecord, MileLogUpdate, MileLogLat, MileLogLong, MileLogPicPath, MileLogTypeCode);
    }

    public void updateShipmentPicture(String invoiceCode, long ShipPicUpdate, List<String> ShipPicPath, int ShipPicTypeCode){
        repository.updateShipmentPicture(invoiceCode, ShipPicUpdate, ShipPicPath, ShipPicTypeCode);
    }

    // เพิ่มเมธอดสำหรับดึง invoice ตาม tripCode
    public LiveData<List<Invoice>> getinvoiceByTrip(Context context) {
        String tripCode = SharedPreferencesHelper.getTrip(context);
        Log.d("Repository", "tripCode:"+tripCode);
        return Transformations.map(getInvoiceList(), invoiceList -> {
            if (invoiceList == null) return new ArrayList<>();
            List<Invoice> filteredList = new ArrayList<>();
            for (Invoice invoice : invoiceList) {
                if (invoice.getTripCode().equals(tripCode)) {
                    filteredList.add(invoice);
                }
            }
            return filteredList;
        });
    }

//    public void insertIssue(Issue issue){
//        repository.insertIssue(issue);
//    }

}
