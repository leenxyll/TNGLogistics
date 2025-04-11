package com.example.tnglogistics.Model;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.tnglogistics.Controller.GeocodeHelper;
import com.example.tnglogistics.Controller.SharedPreferencesHelper;
import com.example.tnglogistics.Network.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {
    private static final String TAG = "Repository";
    private final InvoiceDao invoiceDao;
    private final InvoiceShipLogDao invoiceShipLogDao;
    private final TripShipLogDao tripShipLogDao;
    private final MileLogDao mileLogDao;
    private final EmployeeDao employeeDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Context context;

    public Repository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
        this.invoiceShipLogDao = db.invoiceShipLogDao();
        this.tripShipLogDao = db.tripShipLogDao();
        this.mileLogDao = db.mileLogDao();
        this.employeeDao = db.employeeDao();
        this.context = context;
//        allInvoicesGrouped = invoiceDao.getInvoicesGroupedByLocation();
    }

    public LiveData<List<Invoice>> getAllInvoice() {
        return invoiceDao.getAllInvoice();
    }

    public LiveData<List<Invoice>> getInvoiceBySeq(){
        return invoiceDao.getInvoiceWithMinSeq();
    }

    public Invoice getInvoiceByGeofenceID(String GeofenceID){
        return invoiceDao.getInvoiceByGeofence(GeofenceID);
    }

    public LiveData<Integer> countInvoicesWithStatusFour(){
        return invoiceDao.countInvoicesWithStatusFour();
    }

    public LiveData<Integer> countInvoicesWithStatusFive(){
        return invoiceDao.countInvoicesWithStatusFive();
    }

    public void clearAllData() {
        // ล้างข้อมูลในฐานข้อมูล
        executorService.execute(() -> invoiceDao.deleteAll());// สมมติว่าคุณมี DAO ที่มีเมธอด deleteAll()

        // หรือถ้าคุณมีการเชื่อมต่อกับ API
    }

    public void insertEmployee(Employee employee){
        executorService.execute(() -> employeeDao.insertEmployee(employee));
    }

    public LiveData<Employee> getEmployeeByEmpCode(int EmpCode){
        return employeeDao.getEmployeeByEmpCode(EmpCode);
    }
    public void getShipmentList(Context context, final StatusCallback callback) {
        int EmpCode = SharedPreferencesHelper.getEmployee(context);
        Log.d(TAG, "EmpCode: "+EmpCode);
        RetrofitClient.getInstance().getApiService().getShipmentList(EmpCode).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                boolean status = false;
                if (response.isSuccessful()) {
                    JsonObject responseBody = response.body();
                    if (responseBody != null) {
                        status = responseBody.get("status").getAsBoolean();
                        String message = responseBody.get("message").getAsString();
                        if (status) {
                            Log.d(TAG, "getInvoice Success: " + responseBody.toString());
                            JsonArray invoices = responseBody.get("invoice").getAsJsonArray();
                            Log.d(TAG, "invoice: " + invoices.toString());
                            for(JsonElement invoice: invoices){
                                JsonObject invoiceObj = invoice.getAsJsonObject();
                                Invoice newInvoice = new Invoice();

                                // ShipListSeq
                                JsonElement shipListSeqElement = invoiceObj.get("ShipListSeq");
                                Log.d(TAG, "shipListSeqElement: "+shipListSeqElement);
                                if (shipListSeqElement != null && !shipListSeqElement.isJsonNull()) {
                                    newInvoice.setShipListSeq(shipListSeqElement.getAsInt());
                                }

                                // TripCode
                                JsonElement tripCodeElement = invoiceObj.get("TripCode");
                                Log.d(TAG, "tripCodeElement: "+tripCodeElement);
                                if (tripCodeElement != null && !tripCodeElement.isJsonNull()) {
                                    newInvoice.setTripCode(tripCodeElement.getAsString());
                                    SharedPreferencesHelper.saveTrip(context, tripCodeElement.getAsString());
                                }
                                // หาก shipListSeqElement เป็น null หรือ isJsonNull() จะไม่ถูก set

                                // ShipListInvoiceCode
                                JsonElement shipListInvoiceCodeElement = invoiceObj.get("ShipListInvoiceCode");
                                if (shipListInvoiceCodeElement != null && !shipListInvoiceCodeElement.isJsonNull()) {
                                    newInvoice.setInvoiceCode(shipListInvoiceCodeElement.getAsString());
                                }

                                // InvoiceCusCode
                                JsonElement invoiceCusCodeElement = invoiceObj.get("InvoiceCusCode");
                                if (invoiceCusCodeElement != null && !invoiceCusCodeElement.isJsonNull()) {
                                    newInvoice.setInvoiceCusCode(invoiceCusCodeElement.getAsString());
                                }

                                // CusName
                                JsonElement cusNameElement = invoiceObj.get("CusName");
                                if (cusNameElement != null && !cusNameElement.isJsonNull()) {
                                    newInvoice.setCusName(cusNameElement.getAsString());
                                }

                                // InvoiceShipLoCode
                                JsonElement invoiceShipLoCodeElement = invoiceObj.get("InvoiceShipLoCode");
                                if (invoiceShipLoCodeElement != null && !invoiceShipLoCodeElement.isJsonNull()) {
                                    newInvoice.setInvoiceShipLoCode(invoiceShipLoCodeElement.getAsInt());
                                }

                                // InvoiceShipLoAddr
                                JsonElement invoiceShipLoAddrElement = invoiceObj.get("ShipLoAddr");
                                Log.d(TAG, "ShipLoAddr: "+invoiceShipLoAddrElement);
                                if (invoiceShipLoAddrElement != null && !invoiceShipLoAddrElement.isJsonNull()) {
                                    newInvoice.setShipLoAddr(invoiceShipLoAddrElement.getAsString());
                                }


                                // ตรวจสอบ ShipLoLat และ ShipLoLong
                                JsonElement shipLoLatElement = invoiceObj.get("ShipLoLat");
                                JsonElement shipLoLongElement = invoiceObj.get("ShipLoLong");

                                if (shipLoLatElement != null && !shipLoLatElement.isJsonNull() &&
                                        shipLoLongElement != null && !shipLoLongElement.isJsonNull()) {

                                    // ✅ ใช้ค่าที่มีอยู่
                                    try {
                                        newInvoice.setShipLoLat(shipLoLatElement.getAsDouble());
                                        newInvoice.setShipLoLong(shipLoLongElement.getAsDouble());
                                    } catch (NumberFormatException e) {
                                        Log.w(TAG, "Invalid Lat/Lng values: " + shipLoLatElement + ", " + shipLoLongElement);
                                    }

                                    // บันทึกลง Database
//                                    executorService.execute(() -> invoiceDao.insertInvoice(newInvoice));

                                } else {
                                    // ❌ ไม่มีค่าพิกัด → ใช้ GeocodeHelper หา LatLng
//                                    String address = newInvoice.getShipLoAddr();
//                                    Log.d(TAG, "Addr: "+ address);
//                                    GeocodeHelper.getLatLngAsync(context, address, new GeocodeHelper.GeocodeCallback() {
//                                        @Override
//                                        public void onAddressFetched(LatLng latLng) {
//                                            if (latLng != null) {
//                                                Log.d(TAG, "Fetched LatLng: " + latLng.latitude + ", " + latLng.longitude);
//
//                                                // ✅ อัปเดตค่า LatLng
//                                                newInvoice.setShipLoLat(latLng.latitude);
//                                                newInvoice.setShipLoLong(latLng.longitude);
//                                                executorService.execute(() -> invoiceDao.update(newInvoice));
//                                            } else {
//                                                newInvoice.setShipLoLat(0.0);
//                                                newInvoice.setShipLoLong(0.0);
//                                                executorService.execute(() -> invoiceDao.update(newInvoice));
//                                                Log.e(TAG, "Cannot fetch LatLng for: " + address);
//                                            }
//                                        }
//                                    });
                                    newInvoice.setShipLoLat(0.0);
                                    newInvoice.setShipLoLong(0.0);
                                    executorService.execute(() -> invoiceDao.update(newInvoice));
                                    Log.e(TAG, "Cannot fetch LatLng");
                                }


                                // InvoiceReceiverName
                                JsonElement invoiceReceiverNameElement = invoiceObj.get("InvoiceReceiverName");
                                if (invoiceReceiverNameElement != null && !invoiceReceiverNameElement.isJsonNull()) {
                                    newInvoice.setInvoiceReceiverName(invoiceReceiverNameElement.getAsString());
                                }

                                // InvoiceReceiverPhone
                                JsonElement invoiceReceiverPhoneElement = invoiceObj.get("InvoiceReceiverPhone");
                                if (invoiceReceiverPhoneElement != null && !invoiceReceiverPhoneElement.isJsonNull()) {
                                    newInvoice.setInvoiceReceiverPhone(invoiceReceiverPhoneElement.getAsString());
                                }

                                // InvoiceNote
                                JsonElement invoiceNoteElement = invoiceObj.get("InvoiceNote");
                                if (invoiceNoteElement != null && !invoiceNoteElement.isJsonNull()) {
                                    newInvoice.setInvoiceNote(invoiceNoteElement.getAsString());
                                }

                                newInvoice.setAddGeofence(false);
                                executorService.execute(() -> invoiceDao.insertInvoice(newInvoice));
                                Log.d(TAG, "invoiceList: " + invoice.toString());
                                Log.d(TAG, "invoiceObj: " + invoiceObj.get("ShipListSeq").getAsInt());
                            }
                        } else {
                            Log.e(TAG, "getInvoice Failed: " + responseBody.toString());
                        }
                    } else {
                        Log.e(TAG, "Login Response Body is null");
                    }
                } else {
                    Log.e(TAG, "getInvoice Failed with status: " + response.code());
                }

                // เรียกใช้ callback ส่งค่าผลลัพธ์กลับ
                callback.onStatusReceived(status);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "getInvoice Error: " + t.getMessage());
                callback.onStatusReceived(false);
            }
        });
    }

    // สร้าง interface สำหรับ callback
    public interface StatusCallback {
        void onStatusReceived(boolean status);
    }


//    public void getShipmentList(Context context){
//        int EmpCode = SharedPreferencesHelper.getEmployee(context);
//        Log.d(TAG, "EmpCode: "+EmpCode);
//        RetrofitClient.getInstance().getApiService().getShipmentList(EmpCode).enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                if (response.isSuccessful()) {
//                    JsonObject responseBody = response.body();
//                    if (responseBody != null) {
//                        Boolean status = responseBody.get("status").getAsBoolean();
//                        String message = responseBody.get("message").getAsString();
//                        if (status) {
////                            Toast.makeText(getContext(), "เข้าสู่ระบบสำเร็จ: " + message, Toast.LENGTH_SHORT).show();
//                            // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
//                            Log.d(TAG, "getInvoice Success: " + responseBody.toString());
//                            JsonArray invoices = responseBody.get("invoice").getAsJsonArray();
//                            Log.d(TAG, "invoice: " + invoices.toString());
//                            for(JsonElement invoice: invoices){
//                                JsonObject invoiceObj = invoice.getAsJsonObject();
//                                Invoice newInvoice = new Invoice();
//
//                                // ShipListSeq
//                                JsonElement shipListSeqElement = invoiceObj.get("ShipListSeq");
//                                Log.d(TAG, "shipListSeqElement: "+shipListSeqElement);
//                                if (shipListSeqElement != null && !shipListSeqElement.isJsonNull()) {
//                                    newInvoice.setShipListSeq(shipListSeqElement.getAsInt());
//                                }
//
//                                // TripCode
//                                JsonElement tripCodeElement = invoiceObj.get("TripCode");
//                                Log.d(TAG, "tripCodeElement: "+tripCodeElement);
//                                if (tripCodeElement != null && !tripCodeElement.isJsonNull()) {
//                                    newInvoice.setTripCode(tripCodeElement.getAsString());
//                                    SharedPreferencesHelper.saveTrip(context, tripCodeElement.getAsString());
//                                }
//                                // หาก shipListSeqElement เป็น null หรือ isJsonNull() จะไม่ถูก set
//
//                                // ShipListInvoiceCode
//                                JsonElement shipListInvoiceCodeElement = invoiceObj.get("ShipListInvoiceCode");
//                                if (shipListInvoiceCodeElement != null && !shipListInvoiceCodeElement.isJsonNull()) {
//                                    newInvoice.setInvoiceCode(shipListInvoiceCodeElement.getAsString());
//                                }
//
//                                // InvoiceCusCode
//                                JsonElement invoiceCusCodeElement = invoiceObj.get("InvoiceCusCode");
//                                if (invoiceCusCodeElement != null && !invoiceCusCodeElement.isJsonNull()) {
//                                    newInvoice.setInvoiceCusCode(invoiceCusCodeElement.getAsString());
//                                }
//
//                                // CusName
//                                JsonElement cusNameElement = invoiceObj.get("CusName");
//                                if (cusNameElement != null && !cusNameElement.isJsonNull()) {
//                                    newInvoice.setCusName(cusNameElement.getAsString());
//                                }
//
//                                // InvoiceShipLoCode
//                                JsonElement invoiceShipLoCodeElement = invoiceObj.get("InvoiceShipLoCode");
//                                if (invoiceShipLoCodeElement != null && !invoiceShipLoCodeElement.isJsonNull()) {
//                                    newInvoice.setInvoiceShipLoCode(invoiceShipLoCodeElement.getAsInt());
//                                }
//
//                                // InvoiceShipLoAddr
//                                JsonElement invoiceShipLoAddrElement = invoiceObj.get("ShipLoAddr");
//                                Log.d(TAG, "ShipLoAddr: "+invoiceShipLoAddrElement);
//                                if (invoiceShipLoAddrElement != null && !invoiceShipLoAddrElement.isJsonNull()) {
//                                    newInvoice.setShipLoAddr(invoiceShipLoAddrElement.getAsString());
//                                }
//
//
//                                // ตรวจสอบ ShipLoLat และ ShipLoLong
//                                JsonElement shipLoLatElement = invoiceObj.get("ShipLoLat");
//                                JsonElement shipLoLongElement = invoiceObj.get("ShipLoLong");
//
//                                if (shipLoLatElement != null && !shipLoLatElement.isJsonNull() &&
//                                        shipLoLongElement != null && !shipLoLongElement.isJsonNull()) {
//
//                                    // ✅ ใช้ค่าที่มีอยู่
//                                    try {
//                                        newInvoice.setShipLoLat(shipLoLatElement.getAsDouble());
//                                        newInvoice.setShipLoLong(shipLoLongElement.getAsDouble());
//                                    } catch (NumberFormatException e) {
//                                        Log.w(TAG, "Invalid Lat/Lng values: " + shipLoLatElement + ", " + shipLoLongElement);
//                                    }
//
//                                    // บันทึกลง Database
////                                    executorService.execute(() -> invoiceDao.insertInvoice(newInvoice));
//
//                                } else {
//                                    // ❌ ไม่มีค่าพิกัด → ใช้ GeocodeHelper หา LatLng
//                                    String address = newInvoice.getShipLoAddr();
//                                    Log.d(TAG, "Addr: "+ address);
//                                    GeocodeHelper.getLatLngAsync(context, address, new GeocodeHelper.GeocodeCallback() {
//                                        @Override
//                                        public void onAddressFetched(LatLng latLng) {
//                                            if (latLng != null) {
//                                                Log.d(TAG, "Fetched LatLng: " + latLng.latitude + ", " + latLng.longitude);
//
//                                                // ✅ อัปเดตค่า LatLng
//                                                newInvoice.setShipLoLat(latLng.latitude);
//                                                newInvoice.setShipLoLong(latLng.longitude);
//                                                executorService.execute(() -> invoiceDao.update(newInvoice));
//                                            } else {
//                                                Log.e(TAG, "Cannot fetch LatLng for: " + address);
//                                            }
//                                        }
//                                    });
//                                }
//
//
//                                // InvoiceReceiverName
//                                JsonElement invoiceReceiverNameElement = invoiceObj.get("InvoiceReceiverName");
//                                if (invoiceReceiverNameElement != null && !invoiceReceiverNameElement.isJsonNull()) {
//                                    newInvoice.setInvoiceReceiverName(invoiceReceiverNameElement.getAsString());
//                                }
//
//                                // InvoiceReceiverPhone
//                                JsonElement invoiceReceiverPhoneElement = invoiceObj.get("InvoiceReceiverPhone");
//                                if (invoiceReceiverPhoneElement != null && !invoiceReceiverPhoneElement.isJsonNull()) {
//                                    newInvoice.setInvoiceReceiverPhone(invoiceReceiverPhoneElement.getAsString());
//                                }
//
//                                // InvoiceNote
//                                JsonElement invoiceNoteElement = invoiceObj.get("InvoiceNote");
//                                if (invoiceNoteElement != null && !invoiceNoteElement.isJsonNull()) {
//                                    newInvoice.setInvoiceNote(invoiceNoteElement.getAsString());
//                                }
//
//                                newInvoice.setAddGeofence(false);
//                                executorService.execute(() -> invoiceDao.insertInvoice(newInvoice));
//                                Log.d(TAG, "invoiceList: " + invoice.toString());
//                                Log.d(TAG, "invoiceObj: " + invoiceObj.get("ShipListSeq").getAsInt());
//                            }
//
//                        } else {
////                            Toast.makeText(getContext(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
//                            Log.e(TAG, "getInvoice Failed: " + responseBody.toString());
//                        }
//                    } else {
//                        Log.e(TAG, "Login Response Body is null");
////                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนอง", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Log.e(TAG, "getInvoice Failed with status: " + response.code());
//                    if (response.errorBody() != null) {
//                        try {
//                            clearAllData();
//                            String errorBody = response.errorBody().string();
//                            Log.e(TAG, "getInvoice Error Body: " + errorBody);
//
//                            // แปลง String เป็น JsonObject
//                            JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();
//
//                            // ดึงค่าจาก key "message"
//                            if (errorJson.has("message")) {
//                                String errorMessage = errorJson.get("message").getAsString();
//                                Log.e(TAG, "getInvoice Failed: "+ errorMessage);
////                                Toast.makeText(getContext(), "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
//                            } else {
////                                Toast.makeText(getContext(), "Login Failed: ไม่พบข้อความข้อผิดพลาด", Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (Exception e) {
//                            Log.e(TAG, "Error parsing error body", e);
////                            Toast.makeText(getContext(), "Login Failed: เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e(TAG, "getInvoice Error: " + t.getMessage());
////                Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการเชื่อมต่อ", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

    public void updateInvoice(Invoice invoice){
        executorService.execute(() ->invoiceDao.update(invoice));
    }

    public int getNextMileLogSeq(String tripCode){
        return mileLogDao.getNextMileLogSeq(tripCode);
    }

    public int getNextInvoiceLogSeq(String invoiceCode){
        return invoiceShipLogDao.getNextInvoiceLogSeq(invoiceCode);
    }

    public void updateMile(Context context,int MileLogSeq, int MileLogRecord, long MileLogUpdate, Double MileLogLat, Double MileLogLong, String MileLogPicPath, int MileLogTypeCode){
        Log.d(TAG, "UpdateMile");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(MileLogUpdate));
//        int nextSeq = mileLogDao.getNextMileLogSeq(MileLogTripCode);  // คำนวณ MileLogSeq
        String MileLogTripCode = SharedPreferencesHelper.getTrip(context);
        int TripShipUpdateEmp = SharedPreferencesHelper.getEmployee(context);
        MileLog mileLog = new MileLog();
        mileLog.setMileLogTripCode(MileLogTripCode);
        mileLog.setMileLogRow(MileLogSeq);
        mileLog.setMileLogRecord(MileLogRecord);
        mileLog.setMileLogUpdate(formattedTime);
        mileLog.setMileLogLat(MileLogLat);
        mileLog.setMileLogLong(MileLogLong);
        mileLog.setMileLogPicPath(MileLogPicPath);
        mileLog.setMileLogTypeCode(MileLogTypeCode);
        mileLog.setSynced(false);
        mileLog.setImageSynced(false);
        executorService.execute(() -> {
            int logCount = mileLogDao.checkIfLogExists(MileLogTripCode, MileLogSeq);
            if (logCount == 0) {  // ถ้ายังไม่มี Log นี้อยู่ในฐานข้อมูล
                mileLogDao.insertMileLog(mileLog);
                synMilelogData(TripShipUpdateEmp, mileLog);
            }
        });
    }

    public void synMilelogData(int TripShipUpdateEmp, MileLog log){
        Log.d(TAG, "synMilelogData");
        if (!log.isSynced()) {
            JsonObject mileLog = new JsonObject();
            mileLog.addProperty("MileLogTripCode", log.getMileLogTripCode());
            mileLog.addProperty("MileLogRecord", log.getMileLogRecord());
            Log.d(TAG, "Record: "+log.getMileLogRecord());
            mileLog.addProperty("MileLogUpdate", log.getMileLogUpdate());
            mileLog.addProperty("MileLogLat", log.getMileLogLat());
            mileLog.addProperty("MileLogLong", log.getMileLogLong());
            mileLog.addProperty("MileLogTypeCode", log.getMileLogTypeCode());
            mileLog.addProperty("TripShipUpdateEmp", TripShipUpdateEmp);
            Log.d(TAG, "Emp Update: "+TripShipUpdateEmp);

            RetrofitClient.getInstance().getApiService().insertMileLogData(mileLog).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseBody = response.body();
                        if (responseBody != null) {
                            Boolean status = responseBody.get("status").getAsBoolean();
                            String message = responseBody.get("message").getAsString();
                            int MileLogSeq = responseBody.get("MileLogSeq").getAsInt();
                            if (status) {
//                            Toast.makeText(getContext(), "เข้าสู่ระบบสำเร็จ: " + message, Toast.LENGTH_SHORT).show();
                                // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
                                Log.d(TAG, "insert MileLog Success: " + responseBody.toString());
                                log.setSynced(true);
                                log.setMileLogSeq(MileLogSeq);
                                executorService.execute(() -> mileLogDao.update(log));
                                if(log.getMileLogTypeCode() == 2){
                                    syncMileLogPicture(log);
                                }
//                            executorService.execute(() -> invoiceShipLogDao.markAsSynced(log.getInvoiceShipLogSeq()));
                            } else {
//                            Toast.makeText(getContext(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "insert MileLog Failed: " + responseBody.toString());
                            }
                        } else {
                            Log.e(TAG, "insert MileLog Response Body is null");
//                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนอง", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "insert MileLog Failed with status: " + response.code());
                        if (response.errorBody() != null) {
                            try {
//                                clearAllData();
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "insert MileLog Error Body: " + errorBody);

                                // แปลง String เป็น JsonObject
                                JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                                // ดึงค่าจาก key "message"
                                if (errorJson.has("message")) {
                                    String errorMessage = errorJson.get("message").getAsString();
                                    Log.e(TAG, "insert MileLog Failed: " + errorMessage);
//                                Toast.makeText(getContext(), "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                } else {
//                                Toast.makeText(getContext(), "Login Failed: ไม่พบข้อความข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
//                            Toast.makeText(getContext(), "Login Failed: เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }else{
            Log.d(TAG, "MileLog already synced: " + log.getMileLogSeq());
        }

    }

    public void syncMileLogPicture(MileLog log){
        Log.d(TAG, "syncMileLogPicture");
        if (!log.isImageSynced()) {
            Log.d(TAG,"Unsync MileLog: "+log.getMileLogSeq()+log.isSynced());
            String imagePath = log.getMileLogPicPath();
            File imageFile = new File(imagePath); // แปลง imagePath เป็น File object

            if (!imageFile.exists()) {
                // จัดการกรณีที่ไฟล์ไม่พบ
                Log.e(TAG, "Image file not found at: " + imagePath);
//                Toast.makeText(context, "ไม่พบไฟล์รูปภาพ", Toast.LENGTH_SHORT).show();
                return; // หยุดการทำงาน
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

            RequestBody MileLogTripCodeBody = RequestBody.create(MediaType.parse("text/plain"), log.getMileLogTripCode());
            RequestBody MileLogRecordBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(log.getMileLogRecord()));
            RequestBody MileLogUpdate = RequestBody.create(MediaType.parse("text/plain"), log.getMileLogUpdate());
            RequestBody MileLogSeq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(log.getMileLogSeq()));
            RetrofitClient.getInstance().getApiService().updateMileLogPic(body, MileLogTripCodeBody, MileLogRecordBody, MileLogUpdate, MileLogSeq).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseBody = response.body();
                        if (responseBody != null) {
                            Boolean status = responseBody.get("status").getAsBoolean();
                            String message = responseBody.get("message").getAsString();
                            if (status) {
                                // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
                                Log.d(TAG, "update MileLog Success: " + responseBody.toString());
                                log.setImageSynced(true);
                                executorService.execute(() -> mileLogDao.update(log));
//                            executorService.execute(() -> invoiceShipLogDao.markAsSynced(log.getInvoiceShipLogSeq()));
                            } else {
//                            Toast.makeText(getContext(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "update MileLog Failed: " + responseBody.toString());
                            }
                        } else {
                            Log.e(TAG, "update MileLog Response Body is null");
//                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนอง", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "update MileLog  Failed with status: " + response.code());
                        if (response.errorBody() != null) {
                            try {
//                                clearAllData();
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "update MileLog Error Body: " + errorBody);

                                // แปลง String เป็น JsonObject
                                JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                                // ดึงค่าจาก key "message"
                                if (errorJson.has("message")) {
                                    String errorMessage = errorJson.get("message").getAsString();
                                    Log.e(TAG, "update MileLog Failed: " + errorMessage);
//                                Toast.makeText(getContext(), "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                } else {
//                                Toast.makeText(getContext(), "Login Failed: ไม่พบข้อความข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
//                            Toast.makeText(getContext(), "Login Failed: เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }else{
            Log.d(TAG, "MileLog already synced Data: " + log.getMileLogSeq());
        }
    }

    public List<InvoiceShipLog> getUnsyncedInvoiceShipLogs(){
        return invoiceShipLogDao.getUnsyncedInvoiceShipLogs();
    }

    public  List<MileLog> getUnsyncedMileLogs(){
        return mileLogDao.getUnsyncedMileLogs();
    }

    public  List<MileLog> getUnsyncedMileLogsImage(){
        return mileLogDao.getUnsyncedMileLogsImage();
    }


    public void updateInvoiceStatus(Invoice invoice,int seq, int statusCode, Double lat, Double lng, long timeStamp, Context context){
        Log.d(TAG, "UpdateInvoice");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(timeStamp));
        int EmpCode = SharedPreferencesHelper.getEmployee(context);
        String invoiceCode = invoice.getInvoiceCode();

//        Invoice invoice = invoiceDao.getInvoiceByCode(invoiceCode);
        invoice.setInvoiceShipStatusCode(statusCode);
        Log.d(TAG,"Status Update: "+invoice.getInvoiceShipStatusCode());
        invoice.setInvoiceShipStatusLastUpdate(formattedTime);
        if(invoice.getInvoiceShipStatusCode() == 3){
            invoice.setGeofenceID(null);
        }
        executorService.execute(() -> invoiceDao.update(invoice));

        InvoiceShipLog shipLog = new InvoiceShipLog();
        shipLog.setInvoiceShipLogCode(invoiceCode);
        shipLog.setInvoiceShipLogSeq(seq);
        shipLog.setInvoiceShipLogStatusCode(statusCode);
        shipLog.setInvoiceShipLogUpdate(formattedTime);
        shipLog.setInvoiceShipLogLat(lat);
        shipLog.setInvoiceShipLogLong(lng);
        shipLog.setInvoiceShipLogEmpCode(EmpCode);
        shipLog.setSynced(false);
        executorService.execute(() -> {
            int logCount = invoiceShipLogDao.checkIfLogExists(invoiceCode, seq);
            if (logCount == 0) {  // ถ้ายังไม่มี Log นี้อยู่ในฐานข้อมูล
                invoiceShipLogDao.insertShipLog(shipLog);
                syncInvoiceShipLog(shipLog);
            }
        });
    }

    public void syncInvoiceShipLog(InvoiceShipLog log){
        Log.d(TAG, "syncShipLog");
        if (!log.isSynced()) {
            Log.d(TAG,"Unsync InvoiceCode: "+log.getInvoiceShipLogCode()+log.isSynced());
            RetrofitClient.getInstance().getApiService().updateInvoiceStatus(log).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseBody = response.body();
                        if (responseBody != null) {
                            Boolean status = responseBody.get("status").getAsBoolean();
                            String message = responseBody.get("message").getAsString();
                            if (status) {
//                            Toast.makeText(getContext(), "เข้าสู่ระบบสำเร็จ: " + message, Toast.LENGTH_SHORT).show();
                                // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
                                Log.d(TAG, "update ShipLog Success: " + responseBody.toString());
                                log.setSynced(true);
                                executorService.execute(() -> invoiceShipLogDao.update(log));
//                            executorService.execute(() -> invoiceShipLogDao.markAsSynced(log.getInvoiceShipLogSeq()));
                            } else {
//                            Toast.makeText(getContext(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "update ShipLog Failed: " + responseBody.toString());
                            }
                        } else {
                            Log.e(TAG, "update ShipLog Response Body is null");
//                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนอง", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "update ShipLog  Failed with status: " + response.code());
                        if (response.errorBody() != null) {
                            try {
//                                clearAllData();
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "update ShipLog Error Body: " + errorBody);

                                // แปลง String เป็น JsonObject
                                JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                                // ดึงค่าจาก key "message"
                                if (errorJson.has("message")) {
                                    String errorMessage = errorJson.get("message").getAsString();
                                    Log.e(TAG, "update ShipLog Failed: " + errorMessage);
//                                Toast.makeText(getContext(), "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                } else {
//                                Toast.makeText(getContext(), "Login Failed: ไม่พบข้อความข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
//                            Toast.makeText(getContext(), "Login Failed: เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }else{
            Log.d(TAG, "InvoiceLog already synced: " + log.getInvoiceShipLogCode());
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
