package net.tatans.coeus.network.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

/**
 */
public class SoundPoolUtil implements Runnable {
	private boolean flag = false;
	private String path;
	private Map<Integer, Integer> map;

	private Context context;
	private List<Integer> CurList = new ArrayList<Integer>();;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	private AssetManager assetManager;
	private AssetFileDescriptor soundFile;// ������
	private int CurPlay = 0;

	private AudioManager mgr;
	private float streamVolumeCurrent;
	private float streamVolumeMax;
	private float volume;

	private SoundPlayListener soundPlayListener = new SoundPlayImpl();
	private SoundFileListener soundFileListener = new SoundFileImpl();

	/**
	 * @param String path
	 */
	@SuppressLint("UseSparseArrays")
	public SoundPoolUtil(String path) {
		this.context = TatansApplication.getContext();
		this.path = path;
		assetManager = context.getAssets();
		soundPoolMap = new HashMap<Integer, Integer>();
		mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = streamVolumeCurrent / streamVolumeMax;
		new Thread(this).start();
	}

	/**
	 * @param Map<Integer, Integer> map
	 */
	@SuppressLint("UseSparseArrays")
	public SoundPoolUtil( Map<Integer, Integer> map) {
		this.map = map;
		this.context = TatansApplication.getContext();
		soundPoolMap = new HashMap<Integer, Integer>();
		mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = streamVolumeCurrent / streamVolumeMax;
		new Thread(this).start();
	}

	public void setSoundFileListener(SoundFileListener soundFileListener) {
		this.soundFileListener = soundFileListener;
	}

	public void setSoundPlayListener(SoundPlayListener soundPlayListener) {
		this.soundPlayListener = soundPlayListener;
	}

	@Override
	public void run() {
		 CurList= new ArrayList<Integer>();
		if (path != null) {
			initSound(path);
		} else if (map != null) {
			initSound(map);
		}
	}

	/**
	 * ������Ч��Դ
	 * @param map
	 */
	@SuppressLint("NewApi") 
	private void initSound(Map<Integer, Integer> map) {
		soundPool = new SoundPool(30, AudioManager.STREAM_MUSIC, 100);
		for (Entry<Integer, Integer> m : map.entrySet()) {
			Integer tmp = m.getValue();
			soundPoolMap.put(m.getKey(), soundPool.load(context, tmp, 1));
		}
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				flag = true;
			}
		});
	}

	/**
	 * ������Ч��Դ
	 * @param path
	 */
	@SuppressLint("NewApi")
	private void initSound(String path) {
		try {
			soundPool = new SoundPool(30, AudioManager.STREAM_MUSIC, 100);
			String[] fileNames = assetManager.list(path);
			if (!path.endsWith(File.separator))
				path = path + File.separator;
			soundFileListener.getfileList(fileNames);
			for (int i = 0; i < fileNames.length; i++) {
				soundFile = assetManager.openFd(path + fileNames[i]);
				soundPoolMap.put(i + 1, soundPool.load(soundFile, 1));
			}
			soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId,
						int status) {
					flag = true;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * play (int soundID, float leftVolume, float rightVolume, int priority, int
	 * loop, float rate); ������ 1��Map��ȡֵ 2����ǰ���� Ĭ��Ϊvolume 3��������� Ĭ��Ϊvolume 4�����ȼ�
	 * Ĭ��Ϊ1 5���ز�����Ĭ��Ϊ0 6�������ٶ�Ĭ��Ϊ1f
	 * @param soundtype
	 * @param loop
	 * @param left
	 * @param right
	 * @param rate
	 */
	public void soundPlay(int soundtype, int loop, float left, float right,
			float rate) {
		if (isFlag())
			if (soundPlayListener.playProperty(soundPool, soundPoolMap.get(soundtype), left, right, rate, volume)){
				CurPlay = soundPool.play(soundPoolMap.get(soundtype), volume * left, volume * right, 1, loop, rate);
				CurList.add(CurPlay);
			}
	}

	/**
	 * @param soundtype
	 * @param loop
	 * @param left
	 * @param right
	 */
	public void soundPlay(int soundtype, int loop, float left, float right) {
		soundPlay(soundtype, loop, volume * left, volume * right, 1f);
	}

	/**
	 * @param soundtype
	 * @param loop
	 * @param rate
	 */
	public void soundPlay(int soundtype, int loop, float rate) {
		soundPlay(soundtype, loop, 1, 1, rate);
	}

	/**
	 * @param soundtype
	 * @param loop
	 */
	public void soundPlay(int soundtype, int loop) {
		soundPlay(soundtype, loop, 1f);
	}

	/**
	 * @param soundtype
	 */
	public void soundPlay(int soundtype) {
		soundPlay(soundtype, 0);
	}
	
	
	/**
	 * �ͷ���Ч��Դ
	 * 
	 * @param soundtype
	 * @param left
	 * @param right
	 */
	public void BgsPlay(int soundtype, float left, float right) {
		if (CurPlay != 0)
			soundPool.stop(CurPlay);
		CurPlay = soundPool.play(soundPoolMap.get(soundtype), volume * left,
				volume * right, 1, -1, 1f);
	}

	public void stop() {
		soundPool.stop(CurPlay);
		CurList.remove(soundPool);
	}
	
	public void stopAll(){
		for(Integer cur :CurList){
			soundPool.stop(cur);
		}
		CurList.clear();
	}

	public void ClearBgs() {
		if (CurPlay != 0)
			soundPool.stop(CurPlay);
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public interface SoundPlayListener {
		/**
		 * ���Ի�ȡ��ֵ���в���,�����޸Ĳ鿴����,����true��������Ĭ��ֵ,����true����������Ĭ�ϵ�ֵ;
		 * �������Դ�����ǰ���õ�ֵ,����ʹ�ö�Ӧ��soundPlay()�������������ò���
		 * @param soundPool ����ʹ�øö����������Լ�������
		 * @param soundtype
		 * @param left
		 * @param right
		 * @param rate
		 * @param volume
		 */
		boolean playProperty(SoundPool soundPool, int soundtype, float left,
				float right, float rate, float volume);
	}

	public class SoundPlayImpl implements SoundPlayListener {
		@Override
		public boolean playProperty(SoundPool soundPool, int soundMapType,
				float left, float right, float rate, float volume) {
			return true;
		}
	}

	public interface SoundFileListener {
		void getfileList(String[] file);
	}

	public class SoundFileImpl implements SoundFileListener {
		@Override
		public void getfileList(String[] files) {
			Log.e("TAG", "�ļ���:\t");
			for (String file : files) {
				Log.e("TAG", file + "\t");
			}
		}
	}
}