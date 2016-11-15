package program.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/11/15 0015.
 */
public class PullToRefresh extends ListView implements AbsListView.OnScrollListener {
    private String tage = PullToRefresh.class.getSimpleName();
    private final int PULL_REFRESH = 0;//下拉刷新的状态
    private final int RELEASE_REFRESH = 1;//松开刷新的状态
    private final int REFRESHING = 2;//正在刷新的状态
    private int currentState = PULL_REFRESH;
    private boolean isBootom;//是否到达底部
    private boolean isLoadingMore = false;//是否在加载更多
    private int footViewHeight;//底部局的高度
    private int heareHeight;//头布局的高度，使用getMeasureHeight进行获取，因为getHeight只有在布局全都加载显示完整的时候才获取到数据
    private int downY;//手指按下时的位子
    private int deltaY;//手指移动的偏移量
    private Context context;
    private View footView;
    private ProgressBar heardPb;
    private TextView heardTv;
    private ProgressBar footPb;
    private TextView footTv;
    private View HeardView;
    public Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onPullRefresh();

        void onDownRefresh();
    }

    public PullToRefresh(Context context) {
        this(context, null);
    }

    public PullToRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();

    }

    private void init() {
        initHeardView();
        initFootView();
        setOnScrollListener(this);//初始化监听
    }


    private void initFootView() {
        footView = LayoutInflater.from(context).inflate(R.layout.footview_layout, null);
        footPb = ((ProgressBar) footView.findViewById(R.id.pb));
        footTv = ((TextView) footView.findViewById(R.id.tv));
        //将widthMeasureSpec和heightMeasureSpec分别设置为0，这里的widthMeasureSpec和heightMeasureSpec并不是一个准备的值，而且指定一个规格或者标准让系统帮我们测量View的宽高，当我们指定widthMeasureSpec和heightMeasureSpec分别为0的时候，系统将不采用这个规格去测量，而是根据实际情况去测量。

        footView.measure(0, 0); // 系统会帮我们测量出headerView的高度
        footViewHeight = footView.getMeasuredHeight();
        footView.setPadding(0, -footViewHeight, 0, 0);
        this.addFooterView(footView);//向ListView的顶部添加一个view对象
    }

    private void initHeardView() {
        HeardView = LayoutInflater.from(context).inflate(R.layout.heardview_layout, null);
        heardPb = ((ProgressBar)HeardView.findViewById(R.id.pb));
        heardTv = ((TextView) HeardView.findViewById(R.id.tv));

        HeardView.measure(0, 0); // 系统会帮我们测量出headerView的高度
        heareHeight = HeardView.getMeasuredHeight();
        HeardView.setPadding(0, -heareHeight, 0, 0);
        this.addHeaderView(HeardView);//向ListView的顶部添加一个view对象
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                deltaY = (int) (ev.getY() - downY);
                //当处于滑动状态的时候
                if (currentState == REFRESHING) {
                    break;
                }
                //获取新的padding值
                int topPadding = -heareHeight + deltaY;
                if (topPadding > 0 && getFirstVisiblePosition() == 0) {
                    HeardView.setPadding(0, topPadding, 0, 0);
                    if (topPadding > 0 && currentState == PULL_REFRESH) {
                        //从下拉刷新进入松开刷新状态
                        currentState = RELEASE_REFRESH;
                        //刷新头布局
                        refreshHeaderView();
                    } else if (topPadding > 0 && currentState == RELEASE_REFRESH) {//处于松开收阶段
                        //进入下拉刷新状态
                        currentState = PULL_REFRESH;
                        refreshHeaderView();
                    }
                    return true;//拦截TouchMove，不让listview处理该次move事件,否则会造成listview无法滑动
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == PULL_REFRESH) {
                    //仍处于下拉刷新状态，未滑动一定距离，不加载数据，隐藏headView
                    HeardView.setPadding(0, -heareHeight, 0, 0);
                } else if (currentState == RELEASE_REFRESH) {
                    //滑到一定距离，显示无padding值得headcView
                    HeardView.setPadding(0, 0, 0, 0);
                    currentState = PULL_REFRESH;
                    //刷新头部布局
                    refreshHeaderView();
                    if (listener != null) {
                        //接口回调加载数据
                        listener.onPullRefresh();
                    }
                }
                break;

        }
        return super.onTouchEvent(ev);
    }

    private void refreshHeaderView() {
        switch (currentState) {
            case PULL_REFRESH:
                heardTv.setText("下拉刷新");
                heardPb.setVisibility(GONE);
                break;
            case REFRESHING:
                heardTv.setText("正在刷新");
                heardPb.setVisibility(VISIBLE);
                break;
            case RELEASE_REFRESH:
                heardTv.setText("松开已刷新");
                heardPb.setVisibility(GONE);
                break;
        }
    }

    //完成刷新操作，重置状态,在你获取完数据并更新完adater之后，去在UI线程中调用该方法
    public void completeRefresh() {
        if (isLoadingMore) {
            // 重置footerView状态
            footView.setPadding(0, -footViewHeight, 0, 0);
            isLoadingMore = false;
        } else{
           HeardView.setPadding(0,-heareHeight,0,0);
            isLoadingMore=false;
            currentState=PULL_REFRESH;
            heardPb.setVisibility(GONE);
            heardTv.setText("下拉刷新");
        }
    }
    /**
     * 当滚动状态改变时回调
     * SCROLL_STATE_IDLE:闲置状态，就是手指松开
     * SCROLL_STATE_TOUCH_SCROLL：手指触摸滑动，就是按着来滑动
     * SCROLL_STATE_FLING：快速滑动后松开
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState==SCROLL_STATE_FLING||scrollState==SCROLL_STATE_IDLE){
                if(isBootom&&!isLoadingMore){
                    isLoadingMore=true;
                    footView.setPadding(0,0,0,0);
                    this.setSelection(getCount());
                    if (listener!=null){
                        listener.onDownRefresh();
                    }
                }
            }
    }
    /**
     * 当滚动时调用
     *
     * @param firstVisibleItem
     *            当前屏幕显示在顶部的item的position
     * @param visibleItemCount
     *            当前屏幕显示了多少个条目的总数
     * @param totalItemCount
     *            ListView的总条目的总数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstVisibleItem=getFirstVisiblePosition();
          if (getLastVisiblePosition()==(totalItemCount-1)){
              isBootom=true;
          }else {
              isBootom=false;
          }
    }
}
