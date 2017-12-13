#笔记
1.竖屏笔记: 传笔记的唯一索引 BookLocation
    Intent intent = new Intent();
    ComponentName componentName = new ComponentName("icox.com.scrawlnote",
                            "backups.NoteActivity");
    intent.setComponent(componentName);
    intent.putExtra("BookLocation", "test");
    startActivity(intent);

1.横屏笔记: 传笔记的唯一索引 BookLocation
    Intent intent = new Intent();
    ComponentName componentName = new ComponentName("icox.com.scrawlnote",
                            "icox.com.scrawlnote.NoteLandscapeActivity");
    intent.setComponent(componentName);
    intent.putExtra("BookLocation", "test");
    startActivity(intent);

