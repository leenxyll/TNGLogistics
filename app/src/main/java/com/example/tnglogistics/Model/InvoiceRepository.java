package com.example.tnglogistics.Model;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.room.PrimaryKey;

import com.example.tnglogistics.Controller.GeocodeHelper;
import com.example.tnglogistics.Network.RetrofitClient;
import com.example.tnglogistics.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRepository {
    private static final String TAG = "Repository";
    private final InvoiceDao invoiceDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private LiveData<List<Invoice>> allInvoicesGrouped;

    public InvoiceRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
        allInvoicesGrouped = invoiceDao.getInvoicesGroupedByLocation();
    }

    public LiveData<List<Invoice>> getAllInvoice() {
        return invoiceDao.getAllInvoice();
    }

    public void clearAllData() {
        // ล้างข้อมูลในฐานข้อมูล
        executorService.execute(() -> invoiceDao.deleteAll());// สมมติว่าคุณมี DAO ที่มีเมธอด deleteAll()

        // หรือถ้าคุณมีการเชื่อมต่อกับ API
    }

    public LiveData<List<Invoice>> getInvoicesGroupedByLocation() {
        return allInvoicesGrouped;
    }

    public void getShipmentList(int EmpCode, Context context){
        Log.d(TAG, "EmpCode: "+EmpCode);
        RetrofitClient.getInstance().getApiService().getInvoice(EmpCode).enqueue(new Callback<JsonObject>() {
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
                                    String address = newInvoice.getShipLoAddr();
                                    Log.d(TAG, "Addr: "+ address);
                                    GeocodeHelper.getLatLngAsync(context, address, new GeocodeHelper.GeocodeCallback() {
                                        @Override
                                        public void onAddressFetched(LatLng latLng) {
                                            if (latLng != null) {
                                                Log.d(TAG, "Fetched LatLng: " + latLng.latitude + ", " + latLng.longitude);

                                                // ✅ อัปเดตค่า LatLng
                                                newInvoice.setShipLoLat(latLng.latitude);
                                                newInvoice.setShipLoLong(latLng.longitude);
                                            } else {
                                                Log.e(TAG, "Cannot fetch LatLng for: " + address);
                                            }
                                        }
                                    });
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

                                executorService.execute(() -> invoiceDao.insertInvoice(newInvoice));
                                Log.d(TAG, "invoiceList: " + invoice.toString());
                                Log.d(TAG, "invoiceObj: " + invoiceObj.get("ShipListSeq").getAsInt());
                            }

                        } else {
//                            Toast.makeText(getContext(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "getInvoice Failed: " + responseBody.toString());
                        }
                    } else {
                        Log.e(TAG, "Login Response Body is null");
//                        Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการตอบสนอง", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "getInvoice Failed with status: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            clearAllData();
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "getInvoice Error Body: " + errorBody);

                            // แปลง String เป็น JsonObject
                            JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();

                            // ดึงค่าจาก key "message"
                            if (errorJson.has("message")) {
                                String errorMessage = errorJson.get("message").getAsString();
                                Log.e(TAG, "getInvoice Failed: "+ errorMessage);
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
                Log.e(TAG, "getInvoice Error: " + t.getMessage());
//                Toast.makeText(getContext(), "เกิดข้อผิดพลาดในการเชื่อมต่อ", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
