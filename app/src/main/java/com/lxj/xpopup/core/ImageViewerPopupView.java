package com.lxj.xpopup.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.auto.click.R;
import com.lxj.xpopup.enums.PopupStatus;
import com.lxj.xpopup.interfaces.OnDragChangeListener;
import com.lxj.xpopup.interfaces.OnImageViewerLongPressListener;
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener;
import com.lxj.xpopup.interfaces.XPopupImageLoader;
import com.lxj.xpopup.photoview.PhotoView;
import com.lxj.xpopup.util.PermissionConstants;
import com.lxj.xpopup.util.XPermission;
import com.lxj.xpopup.util.XPopupUtils;
import com.lxj.xpopup.widget.BlankView;
import com.lxj.xpopup.widget.HackyViewPager;
import com.lxj.xpopup.widget.PhotoViewContainer;
import java.util.ArrayList;
import java.util.List;


/**
 * Description: 大图预览的弹窗，使用Transition实现
 * Create by lxj, at 2019/1/22
 */
public class ImageViewerPopupView extends BasePopupView implements OnDragChangeListener, View.OnClickListener {
    protected FrameLayout container;
    protected PhotoViewContainer photoViewContainer;
    protected BlankView placeholderView;
    protected TextView tv_pager_indicator, tv_save;
    protected HackyViewPager pager;
    protected ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    protected List<Object> urls = new ArrayList<>();
    protected XPopupImageLoader imageLoader;
    protected OnSrcViewUpdateListener srcViewUpdateListener;
    protected int position;
    protected Rect rect = null;
    protected ImageView srcView; //动画起始的View，如果为null，移动和过渡动画效果会没有，只有弹窗的缩放功能
    protected PhotoView snapshotView;
    protected boolean isShowPlaceholder = true; //是否显示占位白色，当图片切换为大图时，原来的地方会有一个白色块
    protected int placeholderColor = Color.parseColor("#f1f1f1"); //占位View的颜色
    protected int placeholderStrokeColor = -1; // 占位View的边框色
    protected int placeholderRadius = -1; // 占位View的圆角
    protected boolean isShowSaveBtn = true; //是否显示保存按钮
    protected boolean isShowIndicator = true; //是否页码指示器
    protected boolean isInfinite = false;//是否需要无限滚动
    protected View customView;
    protected int bgColor = Color.rgb(32, 36, 46);//弹窗的背景颜色，可以自定义
    public OnImageViewerLongPressListener longPressListener;

    public ImageViewerPopupView(@NonNull Context context) {
        super(context);
        container = findViewById(R.id.container);
        if (getImplLayoutId() > 0) {
            customView = LayoutInflater.from(getContext()).inflate(getImplLayoutId(), container, false);
            customView.setVisibility(INVISIBLE);
            customView.setAlpha(0);
            container.addView(customView);
        }
    }

    @Override
    final protected int getInnerLayoutId() {
        return R.layout._xpopup_image_viewer_popup_view;
    }

    @Override
    protected void initPopupContent() {
        super.initPopupContent();
        tv_pager_indicator = findViewById(R.id.tv_pager_indicator);
        tv_save = findViewById(R.id.tv_save);
        placeholderView = findViewById(R.id.placeholderView);
        photoViewContainer = findViewById(R.id.photoViewContainer);
        photoViewContainer.setOnDragChangeListener(this);
        pager = findViewById(R.id.pager);
        PhotoViewAdapter photoViewAdapter = new PhotoViewAdapter();
        pager.setAdapter(photoViewAdapter);
        pager.setCurrentItem(position);
        pager.setVisibility(INVISIBLE);
        addOrUpdateSnapshot();
        pager.setOffscreenPageLimit(2);
        pager.addOnPageChangeListener(photoViewAdapter);
        if (!isShowIndicator) tv_pager_indicator.setVisibility(GONE);
        if (!isShowSaveBtn) {
            tv_save.setVisibility(GONE);
        } else {
            tv_save.setOnClickListener(this);
        }
    }

    private void setupPlaceholder() {
        placeholderView.setVisibility(isShowPlaceholder ? VISIBLE : INVISIBLE);
        if (isShowPlaceholder) {
            if (placeholderColor != -1) {
                placeholderView.color = placeholderColor;
            }
            if (placeholderRadius != -1) {
                placeholderView.radius = placeholderRadius;
            }
            if (placeholderStrokeColor != -1) {
                placeholderView.strokeColor = placeholderStrokeColor;
            }
            XPopupUtils.setWidthHeight(placeholderView, rect.width(), rect.height());
            placeholderView.setTranslationX(rect.left);
            placeholderView.setTranslationY(rect.top);
            placeholderView.invalidate();
        }
    }

    private void showPagerIndicator() {
        if (urls.size() > 1) {
            int posi = getRealPosition();
            tv_pager_indicator.setText((posi + 1) + "/" + urls.size());
        }
        if (isShowSaveBtn) tv_save.setVisibility(VISIBLE);
    }

    private void addOrUpdateSnapshot() {
        if (srcView == null) return;
        if (snapshotView == null) {
            snapshotView = new PhotoView(getContext());
            snapshotView.setEnabled(false);
            photoViewContainer.addView(snapshotView);
            snapshotView.setScaleType(srcView.getScaleType());
            snapshotView.setTranslationX(rect.left);
            snapshotView.setTranslationY(rect.top);
            XPopupUtils.setWidthHeight(snapshotView, rect.width(), rect.height());
        }
        int realPosition = getRealPosition();
        snapshotView.setTag(realPosition);
        setupPlaceholder();
        if(imageLoader!=null) imageLoader.loadSnapshot(urls.get(realPosition), snapshotView, srcView);
    }

    @Override
    public void doShowAnimation() {
        if (srcView == null) {
            photoViewContainer.setBackgroundColor(bgColor);
            pager.setVisibility(VISIBLE);
            showPagerIndicator();
            photoViewContainer.isReleasing = false;
            doAfterShow();
            if (customView != null) {
                customView.setAlpha(1f);
                customView.setVisibility(VISIBLE);
            }
            return;
        }
        photoViewContainer.isReleasing = true;
        if (customView != null) customView.setVisibility(VISIBLE);
        snapshotView.setVisibility(VISIBLE);
        doAfterShow();
        snapshotView.post(new Runnable() {
            @Override
            public void run() {
                TransitionManager.beginDelayedTransition((ViewGroup) snapshotView.getParent(), new TransitionSet()
                        .setDuration(getAnimationDuration())
                        .addTransition(new ChangeBounds())
                        .addTransition(new ChangeTransform())
                        .addTransition(new ChangeImageTransform())
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .addListener(new TransitionListenerAdapter() {
                            @Override
                            public void onTransitionEnd(@NonNull Transition transition) {
                                pager.setVisibility(VISIBLE);
                                snapshotView.setVisibility(INVISIBLE);
                                showPagerIndicator();
                                photoViewContainer.isReleasing = false;
                            }
                        }));
                snapshotView.setTranslationY(0);
                snapshotView.setTranslationX(0);
                snapshotView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                XPopupUtils.setWidthHeight(snapshotView, photoViewContainer.getWidth(), photoViewContainer.getHeight());

                // do shadow anim.
                animateShadowBg(bgColor);
                if (customView != null)
                    customView.animate().alpha(1f).setDuration(getAnimationDuration()).start();
            }
        });

    }

    private void animateShadowBg(final int endColor) {
        final int start = ((ColorDrawable) photoViewContainer.getBackground()).getColor();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                photoViewContainer.setBackgroundColor((Integer) argbEvaluator.evaluate(animation.getAnimatedFraction(),
                        start, endColor));
            }
        });
        animator.setDuration(getAnimationDuration())
                .setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    public void doDismissAnimation() {
        if (srcView == null) {
            photoViewContainer.setBackgroundColor(Color.TRANSPARENT);
            doAfterDismiss();
            pager.setVisibility(INVISIBLE);
            placeholderView.setVisibility(INVISIBLE);
            if (customView != null) {
                customView.setAlpha(0f);
                customView.setVisibility(INVISIBLE);
            }
            return;
        }
        tv_pager_indicator.setVisibility(INVISIBLE);
        tv_save.setVisibility(INVISIBLE);
        pager.setVisibility(INVISIBLE);
        photoViewContainer.isReleasing = true;
        snapshotView.setVisibility(VISIBLE);
        snapshotView.post(new Runnable() {
            @Override
            public void run() {
                TransitionManager.beginDelayedTransition((ViewGroup) snapshotView.getParent(), new TransitionSet()
                        .setDuration(getAnimationDuration())
                        .addTransition(new ChangeBounds())
                        .addTransition(new ChangeTransform())
                        .addTransition(new ChangeImageTransform())
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .addListener(new TransitionListenerAdapter() {
                            @Override
                            public void onTransitionStart(@NonNull Transition transition) {
                                super.onTransitionStart(transition);
                                doAfterDismiss();
                            }
                            @Override
                            public void onTransitionEnd(@NonNull Transition transition) {
                                pager.setScaleX(1f);
                                pager.setScaleY(1f);
                                snapshotView.setScaleX(1f);
                                snapshotView.setScaleY(1f);
                                placeholderView.setVisibility(INVISIBLE);
                                snapshotView.setTranslationX(rect.left);
                                snapshotView.setTranslationY(rect.top);
                                XPopupUtils.setWidthHeight(snapshotView, rect.width(), rect.height());
                            }
                        }));

                snapshotView.setScaleX(1f);
                snapshotView.setScaleY(1f);
                snapshotView.setTranslationX(rect.left);
                snapshotView.setTranslationY(rect.top);
                snapshotView.setScaleType(srcView.getScaleType());
                XPopupUtils.setWidthHeight(snapshotView, rect.width(), rect.height());

                // do shadow anim.
                animateShadowBg(Color.TRANSPARENT);
                if (customView != null)
                    customView.animate().alpha(0f).setDuration(getAnimationDuration())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (customView != null) customView.setVisibility(INVISIBLE);
                                }
                            })
                            .start();
            }
        });

    }

    @Override
    public void dismiss() {
        if (popupStatus != PopupStatus.Show) return;
        popupStatus = PopupStatus.Dismissing;
        doDismissAnimation();
    }

    public ImageViewerPopupView setImageUrls(List<Object> urls) {
        this.urls = urls;
        return this;
    }

    public ImageViewerPopupView setSrcViewUpdateListener(OnSrcViewUpdateListener srcViewUpdateListener) {
        this.srcViewUpdateListener = srcViewUpdateListener;
        return this;
    }

    public ImageViewerPopupView setXPopupImageLoader(XPopupImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    /**
     * 是否显示白色占位区块
     *
     * @param isShow
     * @return
     */
    public ImageViewerPopupView isShowPlaceholder(boolean isShow) {
        this.isShowPlaceholder = isShow;
        return this;
    }

    /**
     * 是否显示页码指示器
     *
     * @param isShow
     * @return
     */
    public ImageViewerPopupView isShowIndicator(boolean isShow) {
        this.isShowIndicator = isShow;
        return this;
    }

    /**
     * 是否显示保存按钮
     *
     * @param isShowSaveBtn
     * @return
     */
    public ImageViewerPopupView isShowSaveButton(boolean isShowSaveBtn) {
        this.isShowSaveBtn = isShowSaveBtn;
        return this;
    }

    public ImageViewerPopupView isInfinite(boolean isInfinite) {
        this.isInfinite = isInfinite;
        return this;
    }

    public ImageViewerPopupView setPlaceholderColor(int color) {
        this.placeholderColor = color;
        return this;
    }

    public ImageViewerPopupView setPlaceholderRadius(int radius) {
        this.placeholderRadius = radius;
        return this;
    }

    public ImageViewerPopupView setPlaceholderStrokeColor(int strokeColor) {
        this.placeholderStrokeColor = strokeColor;
        return this;
    }

    public ImageViewerPopupView setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public ImageViewerPopupView setLongPressListener(OnImageViewerLongPressListener longPressListener){
        this.longPressListener = longPressListener;
        return this;
    }

    /**
     * 设置单个使用的源View。单个使用的情况下，无需设置url集合和SrcViewUpdateListener
     *
     * @param srcView
     * @return
     */
    public ImageViewerPopupView setSingleSrcView(ImageView srcView, Object url) {
        if (this.urls == null) {
            urls = new ArrayList<>();
        }
        urls.clear();
        urls.add(url);
        setSrcView(srcView, 0);
        return this;
    }

    public ImageViewerPopupView setSrcView(ImageView srcView, int position) {
        this.srcView = srcView;
        this.position = position;
        if (srcView != null) {
            int[] locations = new int[2];
            this.srcView.getLocationInWindow(locations);
            int left = locations[0] /*- getActivityContentLeft()*/;
            if(XPopupUtils.isLayoutRtl(getContext())){
                left = -(XPopupUtils.getAppWidth(getContext()) - locations[0] - srcView.getWidth());
                rect = new Rect(left, locations[1], left + srcView.getWidth(), locations[1] + srcView.getHeight());
            }else {
                rect = new Rect(left, locations[1], left + srcView.getWidth(), locations[1] + srcView.getHeight());
            }
        }
        return this;
    }

    public void updateSrcView(ImageView srcView) {
        setSrcView(srcView, position);
        addOrUpdateSnapshot();
    }

    @Override
    public void onRelease() {
        dismiss();
    }

    @Override
    public void onDragChange(int dy, float scale, float fraction) {
        tv_pager_indicator.setAlpha(1 - fraction);
        if (customView != null) customView.setAlpha(1 - fraction);
        if (isShowSaveBtn) tv_save.setAlpha(1 - fraction);
        photoViewContainer.setBackgroundColor((Integer) argbEvaluator.evaluate(fraction * .8f, bgColor, Color.TRANSPARENT));
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
        srcView = null;
        srcViewUpdateListener = null;
    }

    @Override
    public void onClick(View v) {
        if (v == tv_save) save();
    }

    @Override
    public void destroy() {
        super.destroy();
        pager.removeOnPageChangeListener((PhotoViewAdapter) pager.getAdapter());
        imageLoader = null;
    }

    protected int getRealPosition(){
        return isInfinite ? position % urls.size() : position;
    }

    /**
     * 保存图片到相册，会自动检查是否有保存权限
     */
    protected void save() {
        XPermission.create(getContext(), PermissionConstants.STORAGE)
                .callback(new XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        XPopupUtils.saveBmpToAlbum(getContext(), imageLoader, urls.get(getRealPosition()));
                    }
                    @Override
                    public void onDenied() { }
                })
                .request();
    }

    public class PhotoViewAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
        @Override
        public int getCount() {
            return isInfinite ? 100000 : urls.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return o == view;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            final int realPosition = isInfinite? position % urls.size() : position;
            //1. build container
            FrameLayout fl = buildContainer(container.getContext());
            ProgressBar progressBar = buildProgressBar(container.getContext());

            //2. add ImageView，maybe PhotoView or SubsamplingScaleImageView
            View view = imageLoader.loadImage(realPosition, urls.get(realPosition), ImageViewerPopupView.this, snapshotView
                    , progressBar);

            //3. add View
            fl.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            //4. add ProgressBar
            fl.addView(progressBar);

            container.addView(fl);
            return fl;
        }

        private FrameLayout buildContainer(Context context){
            FrameLayout fl = new FrameLayout(context);
            fl.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return fl;
        }

        private ProgressBar buildProgressBar(Context context){
            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            int size = XPopupUtils.dp2px(container.getContext(), 40f);
            FrameLayout.LayoutParams params = new LayoutParams(size, size);
            params.gravity = Gravity.CENTER;
            progressBar.setLayoutParams(params);
            progressBar.setVisibility(GONE);
            return progressBar;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
        @Override
        public void onPageSelected(int i) {
            position = i;
            showPagerIndicator();
            //更新srcView
            if (srcViewUpdateListener != null) {
                srcViewUpdateListener.onSrcViewUpdate(ImageViewerPopupView.this, getRealPosition());
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) { }
    }

}
