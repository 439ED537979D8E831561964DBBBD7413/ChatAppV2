package nain.himanshu.chatapp.Adapters;

import android.app.Activity;
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
import nain.himanshu.chatapp.DataModels.ContactModel;
import nain.himanshu.chatapp.R;

public class SelectContactAdapter extends BaseAdapter {

    private List<ContactModel> mList;
    private Context mContext;
    private String USERID;
    private LayoutInflater mInflater;

    public SelectContactAdapter(List<ContactModel> mList, Context mContext, String USERID) {
        this.mList = mList;
        this.mContext = mContext;
        this.USERID = USERID;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
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
        final ContactModel data = mList.get(position);

        View view = convertView;

        if(convertView == null){
            view = mInflater.inflate(R.layout.user_list_item, null);
        }

        CircleImageView mProfilePic = view.findViewById(R.id.profilePic);
        TextView mName = view.findViewById(R.id.name);
        final TextView mCategory = view.findViewById(R.id.category);
        LinearLayout mParentLayout = view.findViewById(R.id.parentLayout);

        if(!data.getProfilePic().isEmpty()){
            Glide.with(mContext)
                    .load(data.getProfilePic())
                    .apply(new RequestOptions()
                            .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                    .into(mProfilePic);
        }else {
            Glide.with(mContext).clear(mProfilePic);
            mProfilePic.setImageResource(R.drawable.demo_photo);
        }

        mName.setText(data.getName());
        /*
        TODO:CHANGE CATEGORY TEXT
         */
        mCategory.setText("");

        mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();

                Bundle bundle = new Bundle();
                bundle.putString("other_name", data.getName());
                bundle.putString("other_pic", data.getProfilePic());
                bundle.putString("other_id", data.getUserId());
                intent.putExtras(bundle);
                Activity activity = (Activity)mContext;
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        });

        return view;
    }
}
