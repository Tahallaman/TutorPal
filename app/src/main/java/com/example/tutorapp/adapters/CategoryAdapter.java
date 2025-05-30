package com.example.tutorapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<String> categories;
    private OnCategoryClickListener listener;
    private Map<String, String> categoryIcons;
    
    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }
    
    public CategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
        initializeCategoryIcons();
    }
    
    private void initializeCategoryIcons() {
        categoryIcons = new HashMap<>();
        categoryIcons.put("Mathematics", "📐");
        categoryIcons.put("Physics", "⚛️");
        categoryIcons.put("Chemistry", "🧪");
        categoryIcons.put("Biology", "🧬");
        categoryIcons.put("English", "📚");
        categoryIcons.put("Computer Science", "💻");
        categoryIcons.put("History", "🏛️");
        categoryIcons.put("Geography", "🌍");
        categoryIcons.put("Music", "🎵");
        categoryIcons.put("Art", "🎨");
        categoryIcons.put("Language", "🗣️");
        categoryIcons.put("Business", "💼");
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category);
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView categoryIcon;
        private TextView categoryName;
        private TextView categoryCount;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryCount = itemView.findViewById(R.id.category_count);
        }
        
        public void bind(String category) {
            categoryName.setText(category);
            
            String icon = categoryIcons.get(category);
            if (icon != null) {
                categoryIcon.setText(icon);
            } else {
                categoryIcon.setText("📖"); // Default icon
            }
            
            // For demo purposes, show random tutor count
            int count = (int) (Math.random() * 100) + 10;
            categoryCount.setText(count + " tutors");
            
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}
