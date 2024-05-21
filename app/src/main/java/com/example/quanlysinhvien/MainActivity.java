package com.example.quanlysinhvien;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText edtMaLop, edtTenLop, edtSiSo;
    Button btnThem, btnSua, btnXoa;
    ListView lv;
    ArrayList<String> mylist;
    ArrayAdapter<String> myadapter;
    SQLiteDatabase mydatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtMaLop = findViewById(R.id.edtMaLop);
        edtTenLop = findViewById(R.id.edtTenLop);
        edtSiSo = findViewById(R.id.edtSiSo);
        btnThem = findViewById(R.id.btnThem);
        btnSua = findViewById(R.id.btnSua);
        btnXoa = findViewById(R.id.btnXoa);
        lv = findViewById(R.id.lv);

        mylist = new ArrayList<>();
        myadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mylist);
        lv.setAdapter(myadapter);

        mydatabase = openOrCreateDatabase("qlsinhvien.db", MODE_PRIVATE, null);
        try {
            String sql = "CREATE TABLE IF NOT EXISTS tbllop(malop TEXT PRIMARY KEY, tenlop TEXT, siso INTEGER)";
            mydatabase.execSQL(sql);
        } catch (Exception e) {
            Log.e("Lỗi", "Table đã tồn tại");
        }

        btnThem.setOnClickListener(view -> {
            String malop = edtMaLop.getText().toString();
            String tenlop = edtTenLop.getText().toString();
            String sisoStr = edtSiSo.getText().toString();

            if (malop.isEmpty() || tenlop.isEmpty() || sisoStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int siso;
            try {
                siso = Integer.parseInt(sisoStr);
                if (siso <= 0) {
                    Toast.makeText(MainActivity.this, "Sỉ số phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Sỉ số phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isMalopExists(malop)) {
                Toast.makeText(MainActivity.this, "Mã lớp đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues myvalue = new ContentValues();
            myvalue.put("malop", malop);
            myvalue.put("tenlop", tenlop);
            myvalue.put("siso", siso);

            String msg;
            if (mydatabase.insert("tbllop", null, myvalue) == -1) {
                msg = "Không thể thêm bản ghi";
            } else {
                msg = "Thêm bản ghi thành công";
                refreshListView();
            }
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        btnXoa.setOnClickListener(view -> {
            String malop = edtMaLop.getText().toString();
            if (malop.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập mã lớp để xoá", Toast.LENGTH_SHORT).show();
                return;
            }
            int n = mydatabase.delete("tbllop", "malop = ?", new String[]{malop});
            String msg;
            if (n == 0) {
                msg = "Không tìm thấy bản ghi để xoá";
            } else {
                msg = "Xoá thành công";
                refreshListView();
            }
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        btnSua.setOnClickListener(view -> {
            String malop = edtMaLop.getText().toString();
            String sisoStr = edtSiSo.getText().toString();

            if (malop.isEmpty() || sisoStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập mã lớp và sỉ số để sửa", Toast.LENGTH_SHORT).show();
                return;
            }

            int siso;
            try {
                siso = Integer.parseInt(sisoStr);
                if (siso <= 0) {
                    Toast.makeText(MainActivity.this, "Sỉ số phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Sỉ số phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues myvalue = new ContentValues();
            myvalue.put("siso", siso);

            int n = mydatabase.update("tbllop", myvalue, "malop = ?", new String[]{malop});
            String msg;
            if (n == 0) {
                msg = "Không tìm thấy bản ghi để sửa";
            } else {
                msg = n + " Bản ghi đã được sửa";
                refreshListView();
            }
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        edtMaLop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterListView(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });

        refreshListView();
    }

    private boolean isMalopExists(String malop) {
        Cursor cursor = mydatabase.query("tbllop", null, "malop = ?", new String[]{malop}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void refreshListView() {
        mylist.clear();
        Cursor c = mydatabase.query("tbllop", null, null, null, null, null, null);
        while (c.moveToNext()) {
            String data = c.getString(0) + " - " + c.getString(1) + " - " + c.getInt(2);
            mylist.add(data);
        }
        c.close();
        myadapter.notifyDataSetChanged();
    }

    private void filterListView(String malop) {
        mylist.clear();
        Cursor c = mydatabase.query("tbllop", null, "malop LIKE ?", new String[]{"%" + malop + "%"}, null, null, null);
        while (c.moveToNext()) {
            String data = c.getString(0) + " - " + c.getString(1) + " - " + c.getInt(2);
            mylist.add(data);
        }
        c.close();
        myadapter.notifyDataSetChanged();
    }
}
