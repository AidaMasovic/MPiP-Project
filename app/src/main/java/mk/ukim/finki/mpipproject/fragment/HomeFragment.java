package mk.ukim.finki.mpipproject.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mk.ukim.finki.mpipproject.R;
import mk.ukim.finki.mpipproject.adapter.EventAdapter;
import mk.ukim.finki.mpipproject.adapter.SwipeController;

public class HomeFragment extends Fragment implements SwipeController.SwipeListener {

    private static final String TAG = "HomeFragment";
    private static final List<String> items = new ArrayList<>(
            Arrays.asList(
                    "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8",
                    "Item 9", "Item 10", "Item 11", "Item 12", "Item 13", "Item 14", "Item 15",
                    "Item 16", "Item 17", "Item 18", "Item 19", "Item 20", "Item 21", "Item 22",
                    "Item 23", "Item 24", "Item 25", "Item 26", "Item 27", "Item 28", "Item 29",
                    "Item 30"
            )
    );

    private EventAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initProperties(view);
    }

    @Override
    public void onSwiped(int itemPosition, SwipeDirection direction) {
        if (itemPosition != -1) {
            if (direction == SwipeDirection.LEFT) {
                items.remove(itemPosition);
                adapter.updateItems(items);
            } else if (direction == SwipeDirection.RIGHT){
                Log.d(TAG, "Item on position " + itemPosition + " has been swiped right");
            }
        }
    }

    private void initProperties(View view) {
        adapter = new EventAdapter(items);

        RecyclerView recyclerView = view.findViewById(R.id.rv_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(adapter);

        SwipeController swipeController = new SwipeController(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}