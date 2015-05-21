package com.leo.plusbutton;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

public class PlusButton extends Button {
	private static final float DEFAULT_ICON_SIZE = 30;
	private float iconSize;
	private float thickness;
	private static final String DEFAULT_ICON_COLOR = "#FFFFFF";
	private int iconColor;
	private static final String DEFAULT_BG_COLOR = "#47bf1b";
	private int bgColor;
	private boolean isColorGradient;
	private static final int ICON_MODE_HOOK = 0;
	private static final int ICON_MODE_CROSS = 1;
	private int iconMode;
	private int startColor;
	private int endColor;
	private OnClickListener onClickListener;
	private Paint linePaint;
	private Paint bgPaint;
	private float rotation;
	private static final int DEFAULT = 0;
	private static final int CONFIRM = 1;
	private int state = DEFAULT;
	private int viewWidth;
	private int viewHeight;
	private static final int ANIM_DURATION = 300;
	private float[] point = new float[8];
	private PointProperty mPropertyPointAX = new XPointProperty(0);
	private PointProperty mPropertyPointAY = new YPointProperty(0);
	private PointProperty mPropertyPointBX = new XPointProperty(1);
	private PointProperty mPropertyPointBY = new YPointProperty(1);
	private PointProperty mPropertyPointCX = new XPointProperty(2);
	private PointProperty mPropertyPointCY = new YPointProperty(2);
	private PointProperty mPropertyPointDX = new XPointProperty(3);
	private PointProperty mPropertyPointDY = new YPointProperty(3);
	private ArgbEvaluator mArgbEvaluator;
	private AnimatorSet changeAnim;
	private AnimatorSet recoveryAnim;

	public PlusButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public PlusButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public PlusButton(Context context) {
		super(context);
		init(null);
	}

	private void init(AttributeSet attrs) {
		TypedArray ta = getResources().obtainAttributes(attrs, R.styleable.PlusButton);
		iconSize = ta.getDimension(R.styleable.PlusButton_icon_size, DEFAULT_ICON_SIZE);
		iconColor = ta.getColor(R.styleable.PlusButton_icon_color, Color.parseColor(DEFAULT_ICON_COLOR));
		isColorGradient = ta.getBoolean(R.styleable.PlusButton_colorGradient, false);
		if(isColorGradient){
			startColor = ta.getColor(R.styleable.PlusButton_startColor, Color.parseColor(DEFAULT_BG_COLOR));
			endColor = ta.getColor(R.styleable.PlusButton_endColor, Color.parseColor(DEFAULT_ICON_COLOR));
			mArgbEvaluator = new ArgbEvaluator();
			bgColor = startColor;
            iconColor = endColor;
		} else{
            Drawable bg = getBackground();
            if(bg instanceof ColorDrawable){
                bgColor = ((ColorDrawable)bg).getColor();
            } else{
                bgColor = Color.parseColor(DEFAULT_BG_COLOR);
            }
		}
		iconMode = ta.getInt(R.styleable.PlusButton_mode, ICON_MODE_HOOK);
		ta.recycle();
		
		iconSize = dp2Px(DEFAULT_ICON_SIZE);
		thickness = iconSize / 10;
		iconColor = Color.parseColor(DEFAULT_ICON_COLOR);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setColor(iconColor);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(thickness);
		linePaint.setStrokeCap(Paint.Cap.SQUARE);
		
		bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setColor(bgColor);

		super.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (state) {
				case DEFAULT:
					change();
					if (onClickListener != null)
						onClickListener.onFirstClick();
					break;
				case CONFIRM:
					recovery();
					if (onClickListener != null)
						onClickListener.onConfirmClick();
					break;
				}
			}
		});
	}
	
	public void change(){
		changeAnim.start();
	}
	
	public void recovery(){
		recoveryAnim.start();
	}
	
    private abstract class PointProperty extends Property<PlusButton, Float> {

        protected int mPointIndex;

        private PointProperty(int pointIndex) {
            super(Float.class, "point_" + pointIndex);
            mPointIndex = pointIndex;
        }
    }

    private class XPointProperty extends PointProperty {

        private XPointProperty(int pointIndex) {
            super(pointIndex);
        }

        @Override
        public Float get(PlusButton object) {
            return object.x(mPointIndex);
        }

        @Override
        public void set(PlusButton object, Float value) {
            object.point[object.xPosition(mPointIndex)] = value;
            object.invalidate();
        }
    }

    private class YPointProperty extends PointProperty {

        private YPointProperty(int pointIndex) {
            super(pointIndex);
        }

        @Override
        public Float get(PlusButton object) {
            return object.y(mPointIndex);
        }

        @Override
        public void set(PlusButton object, Float value) {
            object.point[object.yPosition(mPointIndex)] = value;
            object.invalidate();
        }
    }
    
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		viewWidth = getWidth();
		viewHeight = getHeight();
		point[0] = (viewWidth - iconSize) / 2;
		point[1] = viewHeight / 2;
		point[2] = (viewWidth + iconSize) / 2;
		point[3] = viewHeight / 2;
		point[4] = viewWidth / 2;
		point[5] = (viewHeight - iconSize) / 2;
		point[6] = viewWidth / 2;
		point[7] = (viewHeight + iconSize) / 2;
		
		initAnimator();
		super.onLayout(changed, left, top, right, bottom);
	}
	
	private void initAnimator(){
		initChangeAnim();
		initRecoveryAnim();
	}
	
	@SuppressWarnings("unchecked")
	private ObjectAnimator[] createAnimator(float[] values){
		ObjectAnimator[] anims;
		if(isColorGradient){
			anims = new ObjectAnimator[11];
		} else{
			anims = new ObjectAnimator[9];
		}
		anims[0] = ObjectAnimator.ofFloat(this, mPropertyPointAX, values[0]);
		anims[1] = ObjectAnimator.ofFloat(this, mPropertyPointAY, values[1]);
		anims[2] = ObjectAnimator.ofFloat(this, mPropertyPointBX, values[2]);
		anims[3] = ObjectAnimator.ofFloat(this, mPropertyPointBY, values[3]);
		anims[4] = ObjectAnimator.ofFloat(this, mPropertyPointCX, values[4]);
		anims[5] = ObjectAnimator.ofFloat(this, mPropertyPointCY, values[5]);
		anims[6] = ObjectAnimator.ofFloat(this, mPropertyPointDX, values[6]);
		anims[7] = ObjectAnimator.ofFloat(this, mPropertyPointDY, values[7]);
		anims[8] = ObjectAnimator.ofFloat(this, rotationProperty, 0f, 1f);
		if(isColorGradient){
			anims[9] = ObjectAnimator.ofObject(this, mLineColorProperty, mArgbEvaluator, (int)values[8]);
			anims[10] = ObjectAnimator.ofObject(this, mBackgroundColorProperty, mArgbEvaluator, (int)values[9]);
		}
		return anims;
	}
	
	private void initChangeAnim(){
		changeAnim = new AnimatorSet();
		float[] values = null;
		switch (iconMode) {
		case ICON_MODE_HOOK:
			values = new float[]{(viewWidth - iconSize) / 2,viewHeight / 2, 
					viewWidth / 2, (viewHeight + iconSize) / 2,
					(viewWidth + iconSize) / 2, (viewHeight - iconSize) / 2, 
					viewWidth / 2, (viewHeight + iconSize) / 2, 
					startColor, endColor};
			break;
		case ICON_MODE_CROSS:
            float radius = iconSize / 2;
			values = new float[]{(float)(viewWidth / 2 - Math.sqrt(radius * radius / 2)), (float)(viewHeight / 2 - Math.sqrt(radius * radius / 2)),
                    (float)(viewWidth / 2 + Math.sqrt(radius * radius / 2)), (float)(viewHeight / 2 + Math.sqrt(radius * radius / 2)),
                    (float)(viewWidth / 2 + Math.sqrt(radius * radius / 2)), (float)(viewHeight / 2 - Math.sqrt(radius * radius / 2)),
                    (float)(viewWidth / 2 - Math.sqrt(radius * radius / 2)), (float)(viewHeight / 2 + Math.sqrt(radius * radius / 2)),
					startColor, endColor};
			break;
		}
		changeAnim.playTogether(createAnimator(values));
		changeAnim.setDuration(ANIM_DURATION);
		changeAnim.addListener(animatorListener);
	}
	
	private void initRecoveryAnim(){
		recoveryAnim = new AnimatorSet();
		float[] values = new float[]{(viewWidth - iconSize) / 2, viewHeight / 2, 
				(viewWidth + iconSize) / 2, viewHeight / 2, 
				viewWidth / 2, (viewHeight - iconSize) / 2, 
				viewWidth / 2, (viewHeight + iconSize) / 2,
				endColor, startColor};
		recoveryAnim.playTogether(createAnimator(values));
		recoveryAnim.setDuration(ANIM_DURATION);
		recoveryAnim.addListener(animatorListener);
	}
	
	private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator arg0) {
			setClickable(false);
		}
		
		@Override
		public void onAnimationRepeat(Animator arg0) {}
		
		@Override
		public void onAnimationEnd(Animator arg0) {
			setClickable(true);
			if(state == CONFIRM){
				state = DEFAULT;
			}else {
				state = CONFIRM;
			}
		}
		
		@Override
		public void onAnimationCancel(Animator arg0) {}
	};
	
	private float x(int pointIndex) {
		return point[xPosition(pointIndex)];
	}

	private float y(int pointIndex) {
		return point[yPosition(pointIndex)];
	}

	private int xPosition(int pointIndex) {
		return pointIndex * 2;
	}

	private int yPosition(int pointIndex) {
		return xPosition(pointIndex) + 1;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, viewWidth, viewHeight, bgPaint);

		canvas.save();
		canvas.rotate(180 * rotation, (x(0) + x(1)) / 2, (y(0) + y(1)) / 2);
		canvas.drawLine(x(0), y(0), x(1), y(1), linePaint);
		canvas.restore();

		canvas.save();
		canvas.rotate(180 * rotation, (x(2) + x(3)) / 2, (y(2) + y(3)) / 2);
		canvas.drawLine(x(2), y(2), x(3), y(3), linePaint);
		canvas.restore();
		super.onDraw(canvas);
	}
	
	private Property<PlusButton, Float> rotationProperty = new Property<PlusButton, Float>(Float.class, "rotation") {

		@Override
		public Float get(PlusButton object) {
			return object.rotation;
		}
		@Override
		public void set(PlusButton object, Float value) {
			object.rotation = value;
		};
	};
	
	private Property<PlusButton, Integer> mBackgroundColorProperty = new Property<PlusButton, Integer>(Integer.class, "bg_color") {
        @Override
        public Integer get(PlusButton object) {
            return object.bgPaint.getColor();
        }

        @Override
        public void set(PlusButton object, Integer value) {
            object.bgPaint.setColor(value);
        }
    };

    private Property<PlusButton, Integer> mLineColorProperty = new Property<PlusButton, Integer>(Integer.class, "line_color") {
        @Override
        public Integer get(PlusButton object) {
            return object.linePaint.getColor();
        }

        @Override
        public void set(PlusButton object, Integer value) {
            object.linePaint.setColor(value);
        }
    };
	
	private int dp2Px(float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	public interface OnClickListener {
		public void onFirstClick();

		public void onConfirmClick();
	}
}
