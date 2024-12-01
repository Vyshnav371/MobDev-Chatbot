package com.chatbot.layouttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatbot.layouttest.MessageModal;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatsRV;
    private ImageButton sendMsgIB;
    private EditText userMsgEdt;

    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";

    private ArrayList<MessageModal> messageModalArrayList;
    private MessageRVAdapter messageRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatsRV = findViewById(R.id.idRVChats);
        sendMsgIB = findViewById(R.id.idIBSend);
        userMsgEdt = findViewById(R.id.idEdtMessage);

        messageModalArrayList = new ArrayList<>();

        sendMsgIB.setOnClickListener(v -> {
            if (userMsgEdt.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(userMsgEdt.getText().toString());
            userMsgEdt.setText("");
        });

        messageRVAdapter = new MessageRVAdapter(messageModalArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);

        chatsRV.setLayoutManager(linearLayoutManager);
        chatsRV.setAdapter(messageRVAdapter);
    }

    private void sendMessage(String userMsg) {
        messageModalArrayList.add(new MessageModal(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        String apiKey = "AIzaSyAe8sRY1C1yD1FzalB2VGWdnY2bM45bym4";

        GenerativeModel gm = new GenerativeModel("gemini-pro", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(userMsg).build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String generatedText = result.getText();
                    messageModalArrayList.add(new MessageModal(generatedText, BOT_KEY));
                    runOnUiThread(() -> messageRVAdapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Throwable t) {
                    messageModalArrayList.add(new MessageModal("Sorry, no response found", BOT_KEY));
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "No response from the bot.", Toast.LENGTH_SHORT).show();
                        messageRVAdapter.notifyDataSetChanged();
                    });
                }
            }, getMainExecutor());
        }
    }
}
