package freenet.winterface.freenet;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import freenet.clients.http.bookmark.Bookmark;
import freenet.clients.http.bookmark.BookmarkCategory;
import freenet.clients.http.bookmark.BookmarkItem;
import freenet.clients.http.bookmark.BookmarkManager;
import freenet.keys.FreenetURI;
import freenet.l10n.NodeL10n;
import freenet.node.FSParseException;
import freenet.node.Node;
import freenet.node.useralerts.UserAlertManager;
import freenet.support.URLEncoder;
import freenet.winterface.core.I18n;

public class BookmarkFreenetInterface{

	private BookmarkManager bookmarkManager;
	private Node node;
	private UserAlertManager uam;
	private I18n i18n;

	public BookmarkFreenetInterface(Node node, I18n i18n) {
		this(node, node.clientCore.getBookmarkManager(), i18n);
	}

	@VisibleForTesting
	BookmarkFreenetInterface(Node node, BookmarkManager bookmarkManager, I18n i18n) {
		this.node = node;
		this.bookmarkManager = bookmarkManager;
		this.uam = node.clientCore.alerts;
		this.i18n = i18n;
	}

	public List<BookmarkCategoryWithPath> getBookmarkCategories() {
		List<BookmarkCategoryWithPath> catList = new ArrayList<BookmarkCategoryWithPath>();
		recursivelyAddSubCategoriesWithPath(catList, BookmarkManager.MAIN_CATEGORY, "");
		return catList;
	}

	public int getBookmarkCategoriesCount() {
		return getBookmarkCategories().size();
	}

	private void recursivelyAddSubCategoriesWithPath(List<BookmarkCategoryWithPath> catList, BookmarkCategory bc, String path) {
		BookmarkCategoryWithPath bcWithPath = null;
		try {
			bcWithPath = new BookmarkCategoryWithPath(bc, path);
		} catch (FSParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		catList.add(bcWithPath);
		for (BookmarkCategory bcChild : bc.getSubCategories()) {
			recursivelyAddSubCategoriesWithPath(catList, bcChild, bcWithPath.getCatPath());
		}
	}

	public List<BookmarkItem> getBookmarksFromCat(BookmarkCategory cat) {
		return cat.getItems();
	}

	public int getBookmarksFromCatCount(BookmarkCategory cat) {
		return getBookmarksFromCat(cat).size();
	}

	public BookmarkCategory getCategoryByPath(String path) {
		return bookmarkManager.getCategoryByPath(path);
	}


	public String getBookmarkItemPath(String parentPath, BookmarkItem bmItem) {
		return parentPath + bmItem.getName();
	}

	public String getBookmarkItemPathEncoded(String parentPath, BookmarkItem bmItem) {
		return URLEncoder.encode(getBookmarkItemPath(parentPath, bmItem), false);
	}

	public void editBookmark(String path, String name, FreenetURI key, String descB, String explain, boolean hasAnActivelink) {
		Bookmark bookmark;
		if(path.endsWith("/"))
			bookmark = bookmarkManager.getCategoryByPath(path);
		else
			bookmark = bookmarkManager.getItemByPath(path);

		bookmarkManager.renameBookmark(path, name);
		if(bookmark instanceof BookmarkItem) {
			BookmarkItem item = (BookmarkItem) bookmark;
			item.update(key, hasAnActivelink, descB, explain);
			//TODO Send feeds to Darknet peers
		}
		bookmarkManager.storeBookmarks();
	}
	
	public void addCategory(String path, String name) {
		Bookmark newCategory = null;

		if (name.contains("/")) {
			// Invalid Name
			return;
		} else {
			newCategory = new BookmarkCategory(name);
		}
		
		if (newCategory != null) {
			bookmarkManager.addBookmark(path, newCategory);
			bookmarkManager.storeBookmarks();
		}
	}

	public void addBookmarkItem(String path, String name, FreenetURI key, String descB, String explain, boolean hasAnActivelink) {
		Bookmark newBookmark = null;

		if (name.contains("/")) {
			// Invalid Name
			return;
		} else {
			try {
				newBookmark = new BookmarkItem(key, name, descB, explain, hasAnActivelink, uam);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (newBookmark != null) {
			bookmarkManager.addBookmark(path, newBookmark);
			bookmarkManager.storeBookmarks();
		}
	}

	public void removeBookmark(String path) {
		bookmarkManager.removeBookmark(path);
		bookmarkManager.storeBookmarks();
	}

	public void moveBookmarkUp(String path, boolean store) {
		bookmarkManager.moveBookmarkUp(path, store);
		bookmarkManager.storeBookmarks();
	}

	public void moveBookmarkDown(String path, boolean store) {
		bookmarkManager.moveBookmarkDown(path, store);
		bookmarkManager.storeBookmarks();
	}

	public void storeBookmarks() {
		bookmarkManager.storeBookmarks();
	}
	
	public void reAddDefaultBookmarks() {
		bookmarkManager.reAddDefaultBookmarks();
	}


	// Inner class to store BookmarkCategory and its path
	public class BookmarkCategoryWithPath extends BookmarkCategory {

		private final static String mainCategoryPath = "/";
		private String path;
		private List<BookmarkItem> bookmarkItems = new ArrayList<BookmarkItem>();

		public BookmarkCategoryWithPath(BookmarkCategory bc, String path) throws FSParseException {
			super(bc.getSimpleFieldSet());
			bookmarkItems = bc.getItems();
			if(path.isEmpty()) {
				this.path = mainCategoryPath;
			} else {
				this.path = path + getName() + "/";
			}
		}

		@Override
		public String getVisibleName() {
			if (path.equals(mainCategoryPath)) {
				return i18n.get("Bookmarks.mainCategory") + "(" + mainCategoryPath + ")";
			} else if (name.toLowerCase().startsWith("l10n:")) {
				return NodeL10n.getBase().getString("Bookmarks.Defaults.Name."+name.substring("l10n:".length()));
			} else {
				return name;
			}
		}

		public String getCatPath() {
			return path;
		}

		public String getCatPathEncoded() {
			return URLEncoder.encode(getCatPath(), false);
		}

		public List<BookmarkItem> getItems() {
			return bookmarkItems;
		}

	}

}