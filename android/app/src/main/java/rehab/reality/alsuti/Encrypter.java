package rehab.reality.alsuti;

import android.content.Context;
import android.content.res.AssetManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

/**
 * Created by reality on 3/28/16.
 */
public class Encrypter {
    AssetManager am;
    String cjsString;
    String cliString;
    Context context;

    public Encrypter(Context context) throws IOException {
        this.context = context;
        am = context.getAssets();

        Scanner cjsScanner = new Scanner(am.open("cryptojs.js"));
        cjsString = cjsScanner.useDelimiter("\\A").next();
        cjsScanner.close();

        Scanner cliScanner = new Scanner(am.open("encrypt_file.js"));
        cliString = cliScanner.useDelimiter("\\A").next();
        cliScanner.close();

    }

    public void encryptFile(String fileName, String password, final JsCallback superCallback) throws IOException {
        byte[] b = FileUtils.readFileToByteArray(new File(fileName));

        String content = "YW5kcm9pZHN1Y2tz" + android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);

        final String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

        JsEvaluator jsEvaluator = new JsEvaluator(context);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("var plain = \'" + content.replace("\n", "") + "\';");
        Log.w("text", content.replace("\n", ""));
        stringBuilder.append(cjsString);
        stringBuilder.append(cliString);

        String jsCode = stringBuilder.toString();

        Log.w("js", jsCode);

        try {
            jsEvaluator.callFunction(jsCode, new JsCallback() {
                @Override
                public void onResult(String cipherText) {

                    File outputDir = context.getCacheDir(); // context being the Activity pointer
                    File outputFile = null;
                    try {
                        outputFile = File.createTempFile("alsutiTemp", "." + ext, outputDir);
                        PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
                        writer.print(cipherText);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    Toast.makeText(context, "doing super callback", Toast.LENGTH_LONG).show();
                    superCallback.onResult(outputFile.getAbsolutePath());
                }
            }, "encrypt", password);
        } catch(Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Toast.makeText(context, "are we blocked?", Toast.LENGTH_LONG).show();
    }
}