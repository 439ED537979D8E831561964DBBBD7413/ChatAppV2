package nain.himanshu.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nain.himanshu.chatapp.ChatActivity;
import nain.himanshu.chatapp.DataModels.ConversationData;
import nain.himanshu.chatapp.R;
import nain.himanshu.chatapp.Utils;

public class AllConversationsAdapter extends BaseAdapter {

    private List<ConversationData> mDataList;
    private Context mContext;
    private LayoutInflater mInflater;

    public AllConversationsAdapter(List<ConversationData> mDataList, Context mContext) {
        this.mDataList = mDataList;
        this.mContext = mContext;
        this.mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int POS = position;
        View view = convertView;

        if(convertView == null){
            view = mInflater.inflate(R.layout.conversation_item, null);
        }

        CircleImageView mProfilePic = view.findViewById(R.id.profilePic);
        TextView mName = view.findViewById(R.id.name);
        TextView mMessage = view.findViewById(R.id.latestMessage);
        TextView mTime = view.findViewById(R.id.time);
        LinearLayout mainLayout = view.findViewById(R.id.parentLayout);

        ConversationData data = mDataList.get(position);

        if(!data.getOtherProfilePic().isEmpty()){
            Glide.with(mContext)
                    .load(data.getOtherProfilePic())
                    .apply(new RequestOptions()
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                    .into(mProfilePic);
        }else {
            Glide.with(mContext).clear(mProfilePic);
            mProfilePic.setImageResource(R.drawable.demo_photo);
        }

        mName.setText(data.getOtherName());
        mMessage.setText(data.getLatestMessage());
        mTime.setText(Utils.getTime(data.getTime()));
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConversationData data = mDataList.get(POS);

                Bundle bundle = new Bundle();
                bundle.putString("conversationId", data.getConversationId());
                bundle.putString("other_name", data.getOtherName());
                bundle.putString("other_pic", data.getOtherProfilePic());

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);

            }
        });

        return view;
    }
}
