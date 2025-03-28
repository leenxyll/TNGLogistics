package com.example.tnglogistics.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tnglogistics.Model.Invoice;
import com.example.tnglogistics.Model.InvoiceRepository;
import com.example.tnglogistics.Model.ShipLocation;
import com.example.tnglogistics.Model.ShipLocationRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvoiceViewModel extends AndroidViewModel {
    private static InvoiceViewModel instance;
    private final InvoiceRepository repository;
    private final MediatorLiveData<List<Invoice>> invoiceList = new MediatorLiveData<>();
    private LiveData<List<Invoice>> invoicesGrouped;

    public InvoiceViewModel(Application application) {
        super(application);
        repository = new InvoiceRepository(application);
        invoiceList.addSource(repository.getAllInvoice(), invoiceList::setValue);
        invoicesGrouped = repository.getInvoicesGroupedByLocation();
    }

    public static InvoiceViewModel getInstance(Application application){
        if (instance == null) {
            instance = new InvoiceViewModel(application);
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

    public LiveData<List<Invoice>> getInvoiceList() {
        return invoiceList;
    }

    public void getInvoice(int EmpCode, Context context){
        repository.getShipmentList(EmpCode, context);
    }

    public LiveData<List<Invoice>> getInvoicesGroupedByLocation() {
        return invoicesGrouped;
    }
}
