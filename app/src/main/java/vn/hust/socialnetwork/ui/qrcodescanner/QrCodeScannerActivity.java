package vn.hust.socialnetwork.ui.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.utils.ContextExtension;

public class QrCodeScannerActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;

    private AppCompatImageView ivToolbarBack;
    private CodeScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_qr_code_scanner);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        scannerView = findViewById(R.id.scanner_view);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // init qrcode scanner
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String textResult = result.getText();
                        AlertDialog.Builder builder = new AlertDialog.Builder(QrCodeScannerActivity.this);
                        builder.setTitle(R.string.title_copy_qr_code);
                        builder.setMessage(textResult);
                        builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ContextExtension.copyToClipboard(QrCodeScannerActivity.this, textResult);
                                Toast.makeText(QrCodeScannerActivity.this, R.string.copy_qrcode_success, Toast.LENGTH_SHORT).show();
                                mCodeScanner.startPreview();
                            }
                        });
                        builder.setNegativeButton(R.string.not_agree, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mCodeScanner.startPreview();
                            }
                        });
                        builder.create().show();
                    }
                });
            }
        });
        mCodeScanner.startPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public void finish() {
        mCodeScanner.releaseResources();
        super.finish();
        overridePendingTransition(0, 0);
    }
}