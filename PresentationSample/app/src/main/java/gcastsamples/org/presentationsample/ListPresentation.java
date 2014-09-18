package gcastsamples.org.presentationsample;

import android.app.Presentation;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ListPresentation extends Presentation {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter mAdapter;

    public ListPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getContext();
        Resources r = ctx.getResources();
        setContentView(R.layout.presentation);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(ctx);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    public void add(String data, int imageIndex) {
        mAdapter.add(data, imageIndex);
        mRecyclerView.scrollToPosition(0);
    }

    class Data {
        String text;
        int imageIndex;
        private int src;

        public int getSrc() {
            switch (imageIndex) {
                case 0 : return R.drawable.rj;
                case 1 : return R.drawable.sp;
                case 2 : return R.drawable.bh;
                default: return R.drawable.rj;
            }
        }
    }
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<Data> mDataset;

        public MyAdapter() {
            this.mDataset = new ArrayList<Data>();
        }

        public void add(String text, int imageIndex) {
            Data data = new Data();
            data.text = text;
            data.imageIndex = imageIndex;
            mDataset.add(0, data);
            notifyItemInserted(0);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.content_text);
                mImageView = (ImageView) v.findViewById(R.id.img);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Data data = mDataset.get(position);
            viewHolder.mTextView.setText(data.text);
            Picasso.with(getContext())
                    .load(data.getSrc())
                    .into(viewHolder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
