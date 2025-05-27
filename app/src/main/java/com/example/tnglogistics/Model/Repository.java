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
import java.util.ArrayList;
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
    private final MileLogDao mileLogDao;
    private final EmployeeDao employeeDao;
    private final ShipmentPictureDao shipmentPictureDao;
    private final SubIssueDao subIssueDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Context context;

    public Repository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
        this.invoiceShipLogDao = db.invoiceShipLogDao();
        this.mileLogDao = db.mileLogDao();
        this.employeeDao = db.employeeDao();
        this.shipmentPictureDao = db.shipmentPictureDao();
        this.subIssueDao = db.subIssueDao();
        this.context = context;
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

    public boolean hasMileLogForLocation(String tripCode, int location, int mileTypeCode) {
        return mileLogDao.hasMileLogForLocation(tripCode, location, mileTypeCode) > 0;
    }

    public LiveData<List<Invoice>> getInvoicesByLocation(int shipLoCode){
        return invoiceDao.getInvoicesByLocation(shipLoCode);
    }

    public List<Invoice> getInvoicesByGeofenceID(String geofenceID){
        return invoiceDao.getInvoicesByGeofenceID(geofenceID);
    }

    public LiveData<Integer> countInvoicesWithStatusFour(){
        return invoiceDao.countInvoicesWithStatusFour();
    }

    public LiveData<Integer> countInvoicesWithStatusFive(){
        return invoiceDao.countInvoicesWithStatusFive();
    }

    public void clearAllData() {
        // ล้างข้อมูลในฐานข้อมูล
        executorService.execute(() -> {
            invoiceDao.deleteAll();
            mileLogDao.deleteAll();
            subIssueDao.deleteAll();
        });// สมมติว่าคุณมี DAO ที่มีเมธอด deleteAll()

        // หรือถ้าคุณมีการเชื่อมต่อกับ API
    }

    public LiveData<List<SubIssue>> getSubIssueByIssueTypeCode(int IssueTypeCode){
        return subIssueDao.getSubIssueByIssueTypeCode(IssueTypeCode);
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

                                } else {
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

    public void getSubIssue(){
        RetrofitClient.getInstance().getApiService().getSubIssue().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                boolean status = false;
                if (response.isSuccessful()) {
                    JsonObject responseBody = response.body();
                    if (responseBody != null) {
                        status = responseBody.get("status").getAsBoolean();
                        String message = responseBody.get("message").getAsString();
                        if (status) {
                            Log.d(TAG, "getSubIssue Success: " + responseBody.toString());
                            JsonArray subissues = responseBody.get("subissue").getAsJsonArray();
                            Log.d(TAG, "subissue: " + subissues.toString());
                            for(JsonElement subissue: subissues){
                                JsonObject subissueObj = subissue.getAsJsonObject();
                                SubIssue newSubIssue = new SubIssue();

                                JsonElement subIssueTypeCodeElement = subissueObj.get("SubIssueTypeCode");
                                Log.d(TAG, "subIssueTypeCodeElement: "+subIssueTypeCodeElement);
                                if (subIssueTypeCodeElement != null && !subIssueTypeCodeElement.isJsonNull()) {
                                    newSubIssue.setSubIssueTypeCode(subIssueTypeCodeElement.getAsInt());
                                }

                                JsonElement subIssueTypeNameElement = subissueObj.get("SubIssueTypeName");
                                Log.d(TAG, "subIssueTypeNameElement: "+subIssueTypeNameElement);
                                if (subIssueTypeNameElement != null && !subIssueTypeNameElement.isJsonNull()) {
                                    newSubIssue.setSubIssueTypeName(subIssueTypeNameElement.getAsString());
                                }

                                JsonElement requirePicElement = subissueObj.get("RequirePic");
                                if (requirePicElement != null && !requirePicElement.isJsonNull()) {
                                    newSubIssue.setRequirePic(requirePicElement.getAsString());
                                }

                                JsonElement issueTypeCodeElement = subissueObj.get("IssueTypeCode");
                                if (issueTypeCodeElement != null && !issueTypeCodeElement.isJsonNull()) {
                                    newSubIssue.setIssueTypeCode(issueTypeCodeElement.getAsInt());
                                }

                                executorService.execute(() -> subIssueDao.insertSubIssue(newSubIssue));
                            }
                        } else {
                            Log.e(TAG, "getSubIssue Failed: " + responseBody.toString());
                        }
                    } else {
                        Log.e(TAG, "getSubIssue Response Body is null");
                    }
                } else {
                    Log.e(TAG, "getSubIssue Failed with status: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "getSubIssue Error: " + t.getMessage());
            }
        });
    }

    // สร้าง interface สำหรับ callback
    public interface StatusCallback {
        void onStatusReceived(boolean status);
    }

    public void updateInvoice(Invoice invoice){
        executorService.execute(() ->invoiceDao.update(invoice));
    }

    public int getNextShipListPic(String invoiceCode){
        return shipmentPictureDao.getNextShipPicRow(invoiceCode);
    }

    public int getNextMileLogSeq(String tripCode){
        return mileLogDao.getNextMileLogSeq(tripCode);
    }

    public int getNextInvoiceLogSeq(String invoiceCode){
        return invoiceShipLogDao.getNextInvoiceLogSeq(invoiceCode);
    }

    public void updateMile(Context context,int MileLogSeq, int MileLogRecord, long MileLogUpdate, Double MileLogLat, Double MileLogLong, String MileLogPicPath, int MileLogTypeCode, int MileLogLocation){
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
        mileLog.setMileLogLocation(MileLogLocation); // เพิ่มบรรทัดนี้
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
                                // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
                                Log.d(TAG, "insert MileLog Success: " + responseBody.toString());
                                log.setSynced(true);
                                log.setMileLogSeq(MileLogSeq);
                                executorService.execute(() -> mileLogDao.update(log));
                                if(log.getMileLogTypeCode() == 2){
                                    syncMileLogPicture(log);
                                }
                            } else {
                                Log.e(TAG, "insert MileLog Failed: " + responseBody.toString());
                            }
                        } else {
                            Log.e(TAG, "insert MileLog Response Body is null");
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
                                } else {
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
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
                            } else {
                                Log.e(TAG, "update MileLog Failed: " + responseBody.toString());
                            }
                        } else {
                            Log.e(TAG, "update MileLog Response Body is null");
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
                                } else {
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
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

    public void updateShipmentPicture(String invoiceCode, long ShipPicUpdate, List<String> ShipPicPaths, int ShipPicTypeCode){
        Log.d(TAG, "UpdateShipmentPicture");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(ShipPicUpdate));
        for (String path : ShipPicPaths) {
            executorService.execute(() ->{
                int row = shipmentPictureDao.getNextShipPicRow(invoiceCode);
                Log.d(TAG, "update for path: "+ path);
                ShipmentPicture shipmentPicture = new ShipmentPicture();
                shipmentPicture.setShipPicInvoiceCode(invoiceCode);
                shipmentPicture.setShipPicRow(row);
                shipmentPicture.setShipPicUpdate(formattedTime);
                shipmentPicture.setShipPicPath(path);
                shipmentPicture.setShipPicTypeCode(ShipPicTypeCode);
                shipmentPicture.setSynced(false);
                shipmentPicture.setImageSynced(false);

                int logCount = shipmentPictureDao.checkIfLogExists(invoiceCode, row);
                if (logCount == 0) {  // ถ้ายังไม่มี Log นี้อยู่ในฐานข้อมูล
                    shipmentPictureDao.insertShipPic(shipmentPicture);
                }
            });
        }
    }

    public void syncShipmentPicture(List<ShipmentPicture> shipmentPictureList) {
        if (shipmentPictureList.isEmpty()) return;

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        List<RequestBody> invoiceCodes = new ArrayList<>();
        List<RequestBody> typeCodes = new ArrayList<>();
        List<RequestBody> updateTimes = new ArrayList<>();

        for (ShipmentPicture pic : shipmentPictureList) {
            File imageFile = new File(pic.getShipPicPath());
            if (!imageFile.exists()) continue;

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("images", imageFile.getName(), requestFile);
            imageParts.add(body);

            invoiceCodes.add(RequestBody.create(MediaType.parse("text/plain"), pic.getShipPicInvoiceCode()));
            typeCodes.add(RequestBody.create(MediaType.parse("text/plain"), String.valueOf(pic.getShipPicTypeCode())));
            updateTimes.add(RequestBody.create(MediaType.parse("text/plain"), pic.getShipPicUpdate()));
        }

        RetrofitClient.getInstance().getApiService()
                .uploadShipmentImages(imageParts, invoiceCodes, typeCodes, updateTimes)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            JsonObject responseBody = response.body();
                            if (responseBody != null) {
                                Boolean status = responseBody.get("status").getAsBoolean();
                                String message = responseBody.get("message").getAsString();
                                if (status) {
                                    // ดำเนินการต่อหลังเข้าสู่ระบบสำเร็จ เช่น บันทึก token, ไปหน้าหลัก
                                    Log.d(TAG, "update ShipPic Success: " + responseBody.toString());
                                    for (ShipmentPicture pic : shipmentPictureList) {
                                        pic.setSynced(true);
                                        executorService.execute(() -> shipmentPictureDao.update(pic));
                                    }
                                } else {
                                    Log.e(TAG, "update ShipPic Failed: " + responseBody.toString());
                                }
                            } else {
                                Log.e(TAG, "update ShipPic Response Body is null");
                            }
                        } else {
                            Log.e(TAG, "update ShipPic Failed with status: " + response.code());
                            if (response.errorBody() != null) {
                                try {
//                                clearAllData();
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "update ShipPic Error Body: " + errorBody);

                                    // แปลง String เป็น JsonObject
                                    JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                                    // ดึงค่าจาก key "message"
                                    if (errorJson.has("message")) {
                                        String errorMessage = errorJson.get("message").getAsString();
                                        Log.e(TAG, "update ShipPic Failed: " + errorMessage);
                                    } else {
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing error body", e);
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e(TAG, "Upload ShipPic Error", t);
                    }
                });
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

    public List<ShipmentPicture> getUnsyncedShipPicImage(){
        return shipmentPictureDao.getUnsyncedShipPicImage();
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

    // Overload
    public void updateInvoiceStatus(Invoice invoice,int seq, int statusCode, Double lat, Double lng, long timeStamp, String issueDesc, int issueTypeCode, Context context){
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
        shipLog.setInvoiceShipLogIssueDescription(issueDesc);
        shipLog.setInvoiceShipLogSubCode(issueTypeCode);
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
                                Log.d(TAG, "update ShipLog Success: " + responseBody.toString());
                                log.setSynced(true);
                                executorService.execute(() -> invoiceShipLogDao.update(log));
                            } else {
                                Log.e(TAG, "update ShipLog Failed: " + responseBody.toString());
                            }
                        } else {
                            Log.e(TAG, "update ShipLog Response Body is null");
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
                                } else {
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
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
