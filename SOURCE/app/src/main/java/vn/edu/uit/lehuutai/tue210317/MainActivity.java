package vn.edu.uit.lehuutai.tue210317;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    final String folderPath = Environment.getExternalStorageDirectory() + File.separator + "E3C2";
    ArrayList<Integer> lock;
    String[] CipherText;
    String ClearText;
    EditText edtText;
    Button btnEncrypt, btnDecrypt;
    TextView tvCipherText, tvClearText;
    ImageView imgvLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtText = (EditText) findViewById(R.id.edt_Text);
        btnEncrypt = (Button) findViewById(R.id.btn_Encrypt);
        btnDecrypt = (Button) findViewById(R.id.btn_Decrypt);
        tvCipherText = (TextView) findViewById(R.id.tv_CipherText);
        tvClearText = (TextView) findViewById(R.id.tv_ClearText);
        imgvLock = (ImageView) findViewById(R.id.imgv_lock);

        SupportFunctions obj = new SupportFunctions();
        lock = new ArrayList<Integer>();
        lock.add(R.drawable.lock);
        lock.add(R.drawable.unlock2);

        makeFolder(folderPath);
        WriteSDCard write = new WriteSDCard();
        write.checkExternalMedia();
        edtText.setText("Tai Le Huu, Hang Bui Thi Thanh"
                + "\nStudent of The Faculty of Computer Networks & Communications"
                + "\nUniversity of Information Technology,"
                + "\nVietnam National University of Hochiminh City - VNUHCM");
        btnDecrypt.setEnabled(false);

        BigInteger p = new BigInteger("17", 10);
        BigInteger a = new BigInteger("2", 10);
        BigInteger b = new BigInteger("2", 10);
        if (!obj.isprime(p)) {
            Alert("p = " + p + " is not a prime number");
        }
        final EllipticCurve E = new EllipticCurve(a,b,p);
        if (!E.belongsField()) {
            Alert("a = " + a
                    + "\nor\nb = " + b
                    + "\nnot belong to p = " + p);
        }

        //select a point in the finite field p (original point)
        //Point P = new Point(new BigInteger("7"), new BigInteger("11"));
        final Point P = new Point(new BigInteger("7", 10), new BigInteger("11", 10));
        if (!E.PointbelongsField(P)) {
            Alert("P(x,y) not belong to Field");
        }
        BigInteger rd;
        Point Q;
        do {
            //chooses a secret  integer rd
            rd = new BigInteger(256, new Random());
            //rd = new BigInteger("11");
            //Q = rd.P
            Q = P.kPoint(E, rd);
        } while (Q.isPOSITIVE_INFINITY());



        final Point finalQ = Q;
        btnEncrypt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ElgamalEncryption(E, P, finalQ);
                btnEncrypt.setEnabled(false);
                btnDecrypt.setEnabled(true);
            }
        });

        final BigInteger finalRd = rd;
        btnDecrypt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ElgamalDecryption(E, finalRd, CipherText);
                btnDecrypt.setEnabled(false);
                btnEncrypt.setEnabled(true);
            }
        });
    }

    public void makeFolder(String folderPath) {
        File folder = new File(folderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
            //Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        } else {
            // Do something else on failure
            //Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
        }
    }

    public void  Alert(String mes) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

        dlgAlert.setMessage(mes);
        dlgAlert.setTitle("Error...");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    public void ElgamalEncryption(EllipticCurve E, Point P, Point Q) {
        String plaintext = edtText.getText().toString();
        tvCipherText.setText("");
        tvClearText.setText("");

        Protocols protocol = new Protocols();

        String Enc = "\n----- Encrypt -----\n";
        CipherText = protocol.Encrypt(E, P, Q, plaintext);
        for(String s : CipherText) {
            Enc += "CipherText (Hex): " + new BigInteger(s, 2).toString(16) + "\n";
        }
        tvCipherText.setMovementMethod(new ScrollingMovementMethod());
        tvCipherText.setText(Enc);
        imgvLock.setImageResource(lock.get(0));
        Toast.makeText(MainActivity.this, "Encrypt Success", Toast.LENGTH_SHORT).show();
    }

    public void ElgamalDecryption(EllipticCurve E, BigInteger rd, String[] CipherText) {
        Protocols protocol = new Protocols();

        ClearText = "\n----- Decrypt -----\n";
        ClearText += protocol.Decrypt(E, rd, CipherText);
        tvClearText.setMovementMethod(new ScrollingMovementMethod());
        tvClearText.setText(ClearText);
        imgvLock.setImageResource(lock.get(1));
        Toast.makeText(MainActivity.this, "Decrypt Success", Toast.LENGTH_SHORT).show();
    }
}
