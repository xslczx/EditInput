package com.xslczx.editinput;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.*;

public class EditTextInputImpl extends android.support.v7.widget.AppCompatEditText {
    private OnKeyClickListener mOnKeyClickListener;
    private OnStateListener mOnStateListener;
    private boolean mOnlyCustomChar;

    public EditTextInputImpl(Context context) {
        this(context, null);
    }

    public EditTextInputImpl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextInputImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    /**
     * 只能输入汉字、英文、数字
     */
    public EditTextInputImpl setOnlyCustomChar(boolean onlyCustomChar) {
        mOnlyCustomChar = onlyCustomChar;
        return this;
    }

    public String getTextString() {
        return getText() == null ? "" : getText().toString();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (mOnStateListener != null) {
            mOnStateListener.onFocusChanged(focused);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mOnStateListener != null) {
            mOnStateListener.onTextChanged(text, start, lengthBefore, lengthAfter);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mOnStateListener != null) {
            mOnStateListener.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (mOnKeyClickListener != null
                && mOnKeyClickListener.onTextContextMenuItem(id)) {
            return true;
        }
        return super.onTextContextMenuItem(id);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == 1) {
            Log.d(">>>", "onKeyHide");
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public EditTextInputImpl setOnStateListener(OnStateListener onStateListener) {
        mOnStateListener = onStateListener;
        return this;
    }

    public EditTextInputImpl setOnKeyClickListener(OnKeyClickListener onKeyClickListener) {
        mOnKeyClickListener = onKeyClickListener;
        return this;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection onCreateInputConnection = super.onCreateInputConnection(outAttrs);
        if (onCreateInputConnection == null) return null;
        return new InnerInputConnection(onCreateInputConnection);
    }

    public interface OnKeyClickListener {
        boolean onDeleteClick(CharSequence textBeforeCursor);

        boolean onCommitClick();

        boolean onTextContextMenuItem(int id);
    }

    public interface OnStateListener {

        void onExtractedText(ExtractedText extractedText);

        void onFocusChanged(boolean focused);

        void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter);

        void onSelectionChanged(int selStart, int selEnd);
    }

    class InnerInputConnection extends InputConnectionWrapper {


        public InnerInputConnection(InputConnection target) {
            super(target, true);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                CharSequence textBeforeCursor = null;
                if (!TextUtils.isEmpty(getText())) {
                    textBeforeCursor = getTextBeforeCursor(1, InputConnection.GET_TEXT_WITH_STYLES);
                }
                if (mOnKeyClickListener != null && mOnKeyClickListener.onDeleteClick(textBeforeCursor)) {
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (mOnKeyClickListener != null && mOnKeyClickListener.onCommitClick()) {
                    return true;
                }
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            if (mOnlyCustomChar) {
                if (!text.toString().matches("[\u4e00-\u9fa5]+")
                        && !text.toString().matches("[a-zA-Z ]+")
                        && !text.toString().matches("[0-9]+")) {
                    return false;
                }
            }
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            ExtractedText extractedText = super.getExtractedText(request, flags);
            if (mOnStateListener != null) {
                mOnStateListener.onExtractedText(extractedText);
            }
            return extractedText;
        }

        /**
         * If the given selection is out of bounds, just ignore it.
         * Most likely the text was changed out from under the IME,
         * and the IME is going to have to update all of its state
         * anyway.
         * used for fix <pre>java.lang.IndexOutOfBoundsException</pre> while
         * invoke {@link InputConnection#setSelection(int, int)}
         */
        @Override
        public boolean setSelection(int start, int end) {
            if (start < 0 || end < 0) {
                return true;
            }
            return super.setSelection(start, end);
        }
    }
}
