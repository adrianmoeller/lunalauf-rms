package lunalauf.rms.utilities.network.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FixedObservableQueue<E> {

	private ObservableList<E> list = FXCollections.observableArrayList();

	private final int size;

	public FixedObservableQueue(int size) {
		this.size = size;
	}

	public ObservableList<E> getInternalList() {
		return list;
	}

	public int getSize() {
		return size;
	}

	public void push(E element) {
		list.add(0, element);
		while (list.size() > size)
			list.remove(list.size() - 1);
	}

}
