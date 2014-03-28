package cn.cloudchain.yboxclient.face;

public interface IMenuItemListener {
	public enum ActionType {
		NONE, DELETE, ACCEPT
	}

	public void changeActionMode(ActionType type);

	public void onMenuItemClick(ActionType type);

}
