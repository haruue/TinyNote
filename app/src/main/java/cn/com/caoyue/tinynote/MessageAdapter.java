package cn.com.caoyue.tinynote;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<MessageItem> mData;

    public MessageAdapter(List<MessageItem> date) {
        mData = date;
    }

    public void setData(List<MessageItem> date){
        mData = date;
        notifyDataSetChanged();
    }

    public MessageItem getData(int position) {
        return mData.get(position);
    }

    public OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        boolean onItemLongClick(View view, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.text_message_main)).setText(mData.get(position).getMessage());
        ((TextView) holder.itemView.findViewById(R.id.text_message_time)).setText(mData.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public LinearLayout wholeView;

        public ViewHolder(View itemView) {
            super(itemView);
            wholeView = (LinearLayout) itemView;
            wholeView.setOnClickListener(this);
            wholeView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (itemClickListener != null) {
                return itemClickListener.onItemLongClick(v, getPosition());
            }
            return false;
        }
    }
}
