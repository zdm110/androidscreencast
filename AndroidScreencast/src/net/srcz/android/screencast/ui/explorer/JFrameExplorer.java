package net.srcz.android.screencast.ui.explorer;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.srcz.android.screencast.api.AndroidDevice;
import net.srcz.android.screencast.api.file.FileInfo;

import com.android.ddmlib.IDevice;

public class JFrameExplorer extends JFrame {

	JTree jt;
	JSplitPane jSplitPane;
	IDevice device;
	JList jListFichiers;
	Map<String,List<FileInfo>> cache = new LinkedHashMap<String,List<FileInfo>>();
	
	private class FileTreeNode extends DefaultMutableTreeNode {
		FileInfo fi;

		public FileTreeNode(FileInfo fi) {
			super(fi.name);
			this.fi = fi;
		}

	}

	private class FolderTreeNode extends LazyMutableTreeNode {

		String name;
		String path;

		public FolderTreeNode(String name, String path) {
			this.name = name;
			this.path = path;
		}

		@Override
		public void initChildren() {
			List<FileInfo> fileInfos = cache.get(path);
			if(fileInfos == null)
				fileInfos = new AndroidDevice(device).list(path);
			for (FileInfo fi : fileInfos) {
				if (fi.directory)
					add(new FolderTreeNode(fi.name, path + fi.name + "/"));
				//else
					//add(new FileTreeNode(fi));
			}
		}

		public String toString() {
			return name;
		}

	}

	public JFrameExplorer(IDevice device) {
		this.device = device;
		
		setTitle("Explorer");
		setLayout(new BorderLayout());

		jt = new JTree(new DefaultMutableTreeNode("Test"));
		jt.setModel(new DefaultTreeModel(new FolderTreeNode("Device", "/")));
		jt.setRootVisible(true);
		jt.addTreeSelectionListener(new TreeSelectionListener() {
			
			public void valueChanged(TreeSelectionEvent e) {
				TreePath tp = e.getPath();
				if (tp == null)
					return;
				if (!(tp.getLastPathComponent() instanceof FolderTreeNode))
					return;
				FolderTreeNode node = (FolderTreeNode) tp
						.getLastPathComponent();
				displayFolder(node.path);
			}
		});

		JScrollPane jsp = new JScrollPane(jt);

		jListFichiers = new JList();
		jListFichiers.setListData(new Object[] {});
		
		jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                jsp, new JScrollPane(jListFichiers));
		
		add(jSplitPane, BorderLayout.CENTER);
		setSize(640, 480);
		setLocationRelativeTo(null);

		jListFichiers.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index  = jListFichiers.locationToIndex(e.getPoint());
					ListModel dlm = jListFichiers.getModel();
				    FileInfo item = (FileInfo)dlm.getElementAt(index);;
				    launchFile(item);
				}
			}

		});
	}

	private void displayFolder(String path) {
		List<FileInfo> fileInfos = cache.get(path);
		if(fileInfos == null)
			fileInfos = new AndroidDevice(device).list(path);
		
		List<FileInfo> files = new Vector<FileInfo>();
		for(FileInfo fi2 : fileInfos) {
			if(fi2.directory)
				continue;
			files.add(fi2);
		}
		jListFichiers.setListData(files.toArray());
		
	}
	
	private void launchFile(FileInfo node) {
		try {
			File tempFile = node.downloadTemporary();
			Desktop.getDesktop().open(tempFile);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
