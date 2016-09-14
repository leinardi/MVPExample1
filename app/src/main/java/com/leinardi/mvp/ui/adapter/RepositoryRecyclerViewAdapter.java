package com.leinardi.mvp.ui.adapter;

import android.database.Cursor;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leinardi.mvp.R;
import com.leinardi.mvp.model.Repository;

public class RepositoryRecyclerViewAdapter extends CursorRecyclerViewAdapter<RepositoryRecyclerViewAdapter.ViewHolder> {
    private OnItemLongClickListener mListener;

    public RepositoryRecyclerViewAdapter(Cursor cursor, OnItemLongClickListener listener) {
        super(cursor);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.repository_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final Repository repository = new Repository(cursor);

        int color = repository.getFork() ? R.color.white : R.color.primary_light;
        holder.mMainContentLayout.setBackgroundResource(color);
        holder.mName.setText(repository.getName());
        holder.mOwner.setText(repository.getOwner().getLogin());
        holder.mDescription.setText(repository.getDescription());

        holder.mClickableContentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.onItemLongClick(repository);
                return true;
            }
        });

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ViewGroup mMainContentLayout;
        ViewGroup mClickableContentLayout;
        AppCompatTextView mName;
        AppCompatTextView mOwner;
        AppCompatTextView mDescription;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mMainContentLayout = ((ViewGroup) view.findViewById(R.id.itemContentLayout));
            mClickableContentLayout = ((ViewGroup) view.findViewById(R.id.clickableContentLayout));
            mName = ((AppCompatTextView) view.findViewById(R.id.name));
            mOwner = ((AppCompatTextView) view.findViewById(R.id.owner));
            mDescription = ((AppCompatTextView) view.findViewById(R.id.description));
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Repository dummyItem);
    }
}
