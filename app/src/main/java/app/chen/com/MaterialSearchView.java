package app.chen.com;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by ChenLuming on 2017/7/4.
 */

public class MaterialSearchView extends FrameLayout implements Filter.FilterListener {

    private Context mContext;

    private MenuItem mMenuItem;
    //搜索栏是否打开
    private boolean mIsSearchOpen = false;
    private int mAnimationDuration;
    //跳转码
    public static final int REQUEST_VOICE = 9999;
    private Drawable suggestionIcon;

    //Views
    private View mSearchLayout;
    private View mTintView;
    private ListView mSuggestionsListView;
    private EditText mSearchSrcTextView;
    private ImageButton mBackBtn;
    private ImageButton mVoiceBtn;
    private ImageButton mEmptyBtn;
    private RelativeLayout mSearchTopBar;

    //自定义监听
    private OnQueryTextListener mOnQueryChangeListener;
    private SearchViewListener mSearchViewListener;

    private CharSequence mOldQueryText;
    private CharSequence mUserQuery;
    private ListAdapter mAdapter;
    private boolean allowVoiceSearch;
    private boolean submit = false;
    private boolean ellipsize = false;

    public MaterialSearchView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initStyle(attrs, defStyleAttr);
        initSearchView();
    }


    public MaterialSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(@NonNull Context context) {
        super(context);
        mContext = context;

    }


    @Override
    public void onFilterComplete(int count) {
        if (count > 0) {
            showSuggestions();
        } else {
            dismissSuggestions();
        }
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true);
        mSearchLayout = findViewById(R.id.search_layout);
        mSearchTopBar = (RelativeLayout) findViewById(R.id.search_top_bar);
        mSuggestionsListView = (ListView) findViewById(R.id.suggestion_list);
        mSearchSrcTextView = (EditText) findViewById(R.id.searchTextView);
        mBackBtn = (ImageButton) findViewById(R.id.action_up_btn);
        mVoiceBtn = (ImageButton) findViewById(R.id.action_voice_btn);
        mEmptyBtn = (ImageButton) findViewById(R.id.action_empty_btn);
        mTintView = findViewById(R.id.transparent_view);

        mSearchSrcTextView.setOnClickListener(mOnClickListener);
        mBackBtn.setOnClickListener(mOnClickListener);
        mVoiceBtn.setOnClickListener(mOnClickListener);
        mEmptyBtn.setOnClickListener(mOnClickListener);
        mTintView.setOnClickListener(mOnClickListener);

        allowVoiceSearch = false;
        showVoice(true);


    }

    private void initSearchView() {
        mSearchSrcTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSubmitQuery();
                return true;
            }
        });

        mSearchSrcTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserQuery = s;
                startFilter(s);
                MaterialSearchView.this.onTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchSrcTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(mSearchSrcTextView);
                    showSuggestions();
                }
            }
        });
    }

    private void initStyle(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.MaterialSearchView2, defStyleAttr, 0);

        if (a != null) {
            if (a.hasValue(R.styleable.MaterialSearchView2_searchBackground)) {
                setBackground(a.getDrawable(R.styleable.MaterialSearchView2_searchBackground));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_android_textColor)) {
                setTextColor(a.getColor(R.styleable.MaterialSearchView2_android_textColor, 0));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_android_textColorHint)) {
                setHintTextColor(a.getColor(R.styleable.MaterialSearchView2_android_textColorHint, 0));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_android_hint)) {
                setHint(a.getString(R.styleable.MaterialSearchView2_android_hint));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_searchVoiceIcon)) {
                setVoiceIcon(a.getDrawable(R.styleable.MaterialSearchView2_searchVoiceIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_searchCloseIcon)) {
                setCloseIcon(a.getDrawable(R.styleable.MaterialSearchView2_searchCloseIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_searchBackIcon)) {
                setBackIcon(a.getDrawable(R.styleable.MaterialSearchView2_searchBackIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_searchSuggestionBackground)) {
                setSuggestionBackground(a.getDrawable(R.styleable.MaterialSearchView2_searchSuggestionBackground));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_searchSuggestionIcon)) {
                setSuggestionIcon(a.getDrawable(R.styleable.MaterialSearchView2_searchSuggestionIcon));
            }

            if (a.hasValue(R.styleable.MaterialSearchView2_android_inputType)) {
                setInputType(a.getInt(R.styleable.MaterialSearchView2_android_inputType, EditorInfo.TYPE_NULL));
            }

            a.recycle();
        }
    }

    /**
     * 控制是否显示语音按钮
     *
     * @param voiceSearch
     */
    public void setVoiceSearch(boolean voiceSearch) {
        allowVoiceSearch = voiceSearch;
    }


    /**
     * 如果有文字就显示清空按钮隐藏语言图标
     *
     * @param newText
     */
    private void onTextChanged(CharSequence newText) {
        CharSequence text = mSearchSrcTextView.getText();
        mUserQuery = text;
        boolean hasText = !TextUtils.isEmpty(text);
        if (hasText) {
            mEmptyBtn.setVisibility(VISIBLE);
            showVoice(false);
        } else {
            mEmptyBtn.setVisibility(GONE);
            showVoice(true);
        }
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();
    }


    /**
     * 开始根据字符串来筛选
     *
     * @param s
     */
    private void startFilter(CharSequence s) {
        if (mAdapter != null && mAdapter instanceof Filterable) {
            ((Filterable) mAdapter).getFilter().filter(s, MaterialSearchView.this);
        }
    }


    /**
     * 重置
     */
    private void onSubmitQuery() {
        CharSequence query = mSearchSrcTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener == null || !mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                closeSearch();
                mSearchSrcTextView.setText(null);
            }
        }
    }

    /**
     * 显示或者隐藏语言按钮
     *
     * @param show
     */
    public void showVoice(boolean show) {
        if (show && isVoiceAvailable() && allowVoiceSearch) {
            mVoiceBtn.setVisibility(VISIBLE);
        } else {
            mVoiceBtn.setVisibility(GONE);
        }
    }

    /**
     * 对toolbar 上的菜单栏的操作
     *
     * @param menuItem
     */
    public void setMenuItem(MenuItem menuItem) {
        this.mMenuItem = menuItem;
        mMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSearch();
                return true;
            }
        });
    }

    //显示按钮
    public void showSearch() {
        showSearch(true);
    }

    /**
     * 显示语音按钮
     *
     * @param animate 是否显示动画效果
     */
    public void showSearch(boolean animate) {
        if (mIsSearchOpen) {
            return;
        }

        //Request Focus
        mSearchSrcTextView.setText(null);
        mSearchSrcTextView.requestFocus();

        if (animate) {
            setVisibleWithAnimation();
        } else {
            mSearchLayout.setVisibility(VISIBLE);
            if (mSearchViewListener != null) {
                mSearchViewListener.onSearchViewShow();
            }
        }
        mIsSearchOpen = true;
    }


    private void setVisibleWithAnimation() {
        AnimationUtil.AnimationListener animationListener = new AnimationUtil.AnimationListener() {
            @Override
            public boolean onAnimationStart(View view) {
                return false;
            }

            @Override
            public boolean onAnimationEnd(View view) {
                if (mSearchViewListener != null) {
                    mSearchViewListener.onSearchViewShow();
                }
                return false;
            }

            @Override
            public boolean onAnimationCancel(View view) {
                return false;
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchLayout.setVisibility(View.VISIBLE);
            AnimationUtil.reveal(mSearchTopBar, animationListener);

        } else {
            AnimationUtil.fadeInView(mSearchLayout, mAnimationDuration, animationListener);
        }
    }


    private boolean isVoiceAvailable() {
        if (isInEditMode()) {
            return true;
        }
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> attivitys = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return attivitys.size() > 0;
    }


    @Override
    public void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSearchTopBar.setBackground(background);
        } else {
            mSearchTopBar.setBackgroundDrawable(background);
        }
    }

    public void setSuggestionBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSuggestionsListView.setBackground(background);
        } else {
            mSuggestionsListView.setBackgroundDrawable(background);
        }
    }

    /**
     * 给EditText着色
     *
     * @param drawable
     */
    public void setCursorDrawable(int drawable) {
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(mSearchSrcTextView, drawable);
        } catch (Exception ignored) {
            Log.e("MaterialSearchView", ignored.toString());
        }
    }

    public void setTextColor(int color) {
        mSearchSrcTextView.setTextColor(color);
    }

    public void setHintTextColor(int color) {
        mSearchSrcTextView.setHintTextColor(color);
    }

    public void setHint(CharSequence hint) {
        mSearchSrcTextView.setHint(hint);
    }

    public void setVoiceIcon(Drawable drawable) {
        mVoiceBtn.setImageDrawable(drawable);
    }

    public void setCloseIcon(Drawable drawable) {
        mEmptyBtn.setImageDrawable(drawable);
    }

    public void setBackIcon(Drawable drawable) {
        mBackBtn.setImageDrawable(drawable);
    }

    public void setSuggestionIcon(Drawable drawable) {
        suggestionIcon = drawable;
    }

    public void setInputType(int inputType) {
        mSearchSrcTextView.setInputType(inputType);
    }


    private final OnClickListener mOnClickListener = new OnClickListener() {

        public void onClick(View v) {
            if (v == mBackBtn) {
                closeSearch();
            } else if (v == mVoiceBtn) {
                onVoiceClicked();
            } else if (v == mEmptyBtn) {
                mSearchSrcTextView.setText(null);
            } else if (v == mSearchSrcTextView) {
                showSuggestions();
            } else if (v == mTintView) {
                closeSearch();
            }
        }
    };


    /**
     * 关闭搜索
     */
    public void closeSearch() {
        if (!mIsSearchOpen) {
            return;
        }

        mSearchSrcTextView.setText(null);
        dismissSuggestions();
        clearFocus();
        mSearchLayout.setVisibility(GONE);
        if (mSearchViewListener != null) {
            mSearchViewListener.onSearchViewClosed();
        }
        hideKeyboard(mSearchLayout);
        mIsSearchOpen = false;
    }

    public void dismissSuggestions() {
        if (mSuggestionsListView.getVisibility() == VISIBLE) {
            mSuggestionsListView.setVisibility(GONE);
        }
    }


    /**
     * 点击语音 打开的并不是手机软键盘上的语音，而是调用第三方的语音助手
     */
    private void onVoiceClicked() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "hhhhhhhhhh");
            if (mContext instanceof Activity) {
                ((Activity) mContext).startActivityForResult(intent, REQUEST_VOICE);
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "请至少安装一个语音助手", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 显示list
     */
    public void showSuggestions() {
        if (mAdapter != null && mAdapter.getCount() > 0 && mSuggestionsListView.getVisibility() == GONE) {
            mSuggestionsListView.setVisibility(VISIBLE);
        }
    }

    public void setAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        mSuggestionsListView.setAdapter(adapter);
        startFilter(mSearchSrcTextView.getText());
    }

    /**
     * 语音进行搜索
     *
     * @param query  识别到你说的文字
     * @param submit 是否要清空
     */
    public void setQuery(CharSequence query, boolean submit) {
        mSearchSrcTextView.setText(query);
        if (query != null) {
            mSearchSrcTextView.setSelection(mSearchSrcTextView.length());
            mUserQuery = query;
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    /**
     * 使用资源填充adapter
     *
     * @param suggestions
     */
    public void setSuggestions(String[] suggestions) {
        if (suggestions != null && suggestions.length > 0) {
            mTintView.setVisibility(VISIBLE);
            final SearchAdapter adapter = new SearchAdapter(mContext, suggestions, suggestionIcon, ellipsize);
            setAdapter(adapter);

            setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setQuery((String) adapter.getItem(position), submit);
                    hideKeyboard(mSearchLayout);
                }
            });
        } else {
            mTintView.setVisibility(GONE);
        }
    }


    /**
     * 搜索框显示和关闭的回调
     */
    public interface SearchViewListener {
        void onSearchViewShow();

        void onSearchViewClosed();
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    /**
     * 搜索栏目的监听
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSuggestionsListView.setOnItemClickListener(listener);
    }

    public void setSearchViewListener(SearchViewListener mSearchViewListener) {
        this.mSearchViewListener = mSearchViewListener;
    }

    /**
     * 文字的监听
     */
    public interface OnQueryTextListener {

        boolean onQueryTextSubmit(String query);

        boolean onQueryTextChange(String newText);

    }

    //隐藏键盘
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //显示键盘
    public void showKeyboard(View view) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

}
