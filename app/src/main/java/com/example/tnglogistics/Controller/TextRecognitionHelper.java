package com.example.tnglogistics.Controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class TextRecognitionHelper {
    private static String TAG = "ProcessTextRecognition";
    private TextRecognitionListener listener;
    private Bitmap bitmap;
    private StringBuilder result;

    public TextRecognitionHelper(String imagePath, TextRecognitionListener listener){
        this.listener = listener; // เก็บ listener ไว้เพื่อให้แจ้งเมื่อการประมวลผลเสร็จ
        Log.d(TAG, "processTextRecognition Install "+imagePath );
        if (imagePath != null) {
            bitmap = BitmapFactory.decodeFile(imagePath);
        }
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(this::processTextRecognitionResult)
                .addOnFailureListener(e -> Log.e(TAG, e.toString()));
    }

    private void processTextRecognitionResult(Text text) {
        Log.d(TAG, "Call for Result");
        this.result = new StringBuilder();
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element element : line.getElements()) {
                    String detectedText = element.getText();
                    if (detectedText.matches("\\d+")) { // กรองเฉพาะตัวเลข
                        this.result.append(detectedText).append("");
                    }
                }
            }
        }
        Log.d(TAG, "Result is "+result.toString());

        // แจ้งให้ listener รู้ว่าเสร็จแล้ว
        if (listener != null) {
            listener.onTextRecognitionComplete(result.toString());
        }
    }

    public String getResult(){
        if(this.result != null & this.result.length() != 0){
            return this.result.toString();
        } else {
            return null;
        }
    }

    public interface TextRecognitionListener {
        void onTextRecognitionComplete(String result);
    }
}
