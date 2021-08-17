package com.xslczx.editinput;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.ExtractedText;

public class MainActivity extends AppCompatActivity {

    private EditTextInputImpl mEditTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditTextInput = findViewById(R.id.edit_text);
        mEditTextInput.setText("Hello World ！");
        mEditTextInput.setOnStateListener(new EditTextInputImpl.OnStateListener() {

            @Override
            public void onExtractedText(ExtractedText extractedText) {
                Log.d(">>>", "onExtractedText " + extractedText.text + ",start:" + extractedText.selectionStart + ",end:" + extractedText.selectionEnd);
            }

            @Override
            public void onFocusChanged(boolean focused) {
                Log.d(">>>", "onFocusChanged: " + focused);
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                Log.d(">>>", "onTextChanged: " + text + ",start:" + start + ",lengthBefore:" + lengthBefore + ",lengthAfter:" + lengthAfter);
            }

            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                Log.d(">>>", "onSelectionChanged start:" + selStart + ",end:" + selEnd);
            }
        }).setOnKeyClickListener(new EditTextInputImpl.OnKeyClickListener() {
            @Override
            public boolean onDeleteClick(CharSequence textBeforeCursor) {
                Log.d(">>>", "onDeleteClick: " + textBeforeCursor);
                return false;
            }

            @Override
            public boolean onCommitClick() {
                Log.d(">>>", "onCommitClick");
                return false;
            }

            @Override
            public boolean onTextContextMenuItem(int id) {
                final int copy = android.R.id.copy;
                final int selectAll = android.R.id.selectAll;
                final int cut = android.R.id.cut;
                final int paste = android.R.id.paste;
                if (id == copy) {
                    Log.d(">>>", "onTextContextMenuItem: 复制");
                } else if (id == paste) {
                    Log.d(">>>", "onTextContextMenuItem: 粘贴");
                } else if (id == cut) {
                    Log.d(">>>", "onTextContextMenuItem: 剪切");
                } else if (id == selectAll) {
                    Log.d(">>>", "onTextContextMenuItem: 全选");
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                            && id == android.R.id.shareText) {
                        Log.d(">>>", "onTextContextMenuItem: 分享");
                    }
                }
                return false;
            }
        });
    }
}