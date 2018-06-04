package nain.himanshu.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class CustomSwipeRefresh extends SwipeRefreshLayout {

    private ListView mListView;

    public CustomSwipeRefresh(@NonNull Context context) {
        super(context);
    }

    public CustomSwipeRefresh(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTargetView(ListView listView){
        this.mListView = listView;
    }


    @Override
    public boolean canChildScrollUp() {

        if(mListView !=null){

            return mListView.getFirstVisiblePosition()!=0;

            //return (mListView.getChildCount()>0)&&((mListView.getFirstVisiblePosition()>0)||mListView.getChildAt(0).getTop()<0);

        }else {
            return super.canChildScrollUp();
        }

    }
}
