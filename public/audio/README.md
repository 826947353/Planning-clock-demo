# 音频文件放置说明

## 背景音乐 (BGM)

将首页自动播放的背景音乐文件放入此目录：

```
public/audio/bgm/background.mp3
```

- 格式：MP3
- 建议：轻柔、舒缓的纯音乐
- 默认音量：30%
- 自动循环播放

## 白噪音

将白噪音文件放入此目录：

```
public/audio/white-noise/
  ├── rain.mp3    # 雨声
  ├── forest.mp3  # 森林
  ├── waves.mp3   # 海浪
  ├── fire.mp3    # 篝火
  ├── cafe.mp3    # 咖啡馆
  └── wind.mp3    # 微风
```

- 格式：MP3
- 建议：循环播放无突兀断点的音频
- 默认音量：50%

## 获取白噪音资源

推荐以下免费资源网站：

1. ** freesound.org ** - 免费音效库
2. ** pixabay.com ** - 免费音乐和音效
3. ** zapsplat.com ** - 免费音效
4. ** mixkit.co ** - 免费音效

搜索关键词：
- rain sounds / 雨声
- forest ambience / 森林环境音
- ocean waves / 海浪声
- campfire / 篝火声
- cafe ambience / 咖啡馆环境音
- wind sounds / 风声

## 注意事项

- 音频文件会被直接复制到构建输出目录
- 建议文件大小控制在 5MB 以内以保证加载速度
- 白噪音音频建议时长 1-5 分钟并设置为循环播放