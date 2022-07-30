package vn.hust.socialnetwork.ui.report;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.ReportService;
import vn.hust.socialnetwork.utils.ContextExtension;

public class ReportActivity extends AppCompatActivity {

    private ReportService reportService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvReportLabel;
    private TextInputEditText etReportContent;
    private AppCompatButton btnCreateReport;
    private Dialog progressDialog;

    private int type;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            type = extras.getInt("type");
            url = extras.getString("url");
        }

        if (type == 0 || url == null || url.isEmpty()) {
            Toast.makeText(ReportActivity.this, R.string.init_report_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        // api
        reportService = ApiClient.getClient().create(ReportService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvReportLabel = findViewById(R.id.tv_report_label);
        etReportContent = findViewById(R.id.et_report_content);
        btnCreateReport = findViewById(R.id.btn_create_report);

        // init
        progressDialog = ContextExtension.createProgressDialog(ReportActivity.this);
        tvToolbarTitle.setText(R.string.title_toolbar_report);
        if (type == 1) {
            tvReportLabel.setText(R.string.report_post_description);
        } else if (type == 2) {
            tvReportLabel.setText(R.string.report_comment_description);
        } else if (type == 3) {
            tvReportLabel.setText(R.string.report_group_description);
        } else {
            tvReportLabel.setText(R.string.report_description);
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCreateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReport();
            }
        });
    }

    private void createReport() {
        String content = etReportContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(ReportActivity.this, R.string.error_content_report_empty, Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.show();
            Map<String, Object> req = new HashMap<>();
            req.put("type", type);
            req.put("content", content);
            req.put("url", url);
            Call<BaseResponse<String>> call = reportService.createReport(req);
            call.enqueue(new Callback<BaseResponse<String>>() {
                @Override
                public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this, R.style.AlertDialogTheme);
                        builder.setTitle(R.string.title_toolbar_report);
                        builder.setMessage(R.string.report_success);
                        builder.setPositiveButton("Đã hiểu", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        });
                        dialog.show();
                    } else {
                        Toast.makeText(ReportActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                    call.cancel();
                    Toast.makeText(ReportActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }
}