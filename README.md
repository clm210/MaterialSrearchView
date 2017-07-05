# MaterialSearchView-ChenLeon [![Android Arsenal](https://img.shields.io/badge/Android%20MaterialSearchView-Leon%20-green.svg?style=true)](https://android-arsenal.com/details/1/2842)

根据需求写的一个沉浸式的搜索栏，包括根据文字搜索和语言搜索，摸索功能只是模拟的，大家可以根据自己的项目替换数据

感谢 MiguelCatalan 提供的技术分享  [![Android Arsenal](https://img.shields.io/badge/Android%20MaterialSearchView-Leon%Thank_you20-green.svg?style=true)](https://android-arsenal.com/details/1/2842)


![Alt text](/aasa.gif)

Usage
-----
    根据 MiguelCatalan(https://github.com/MiguelCatalan/MaterialSearchView)写的技术分享，
	后续处理了一下，也解决了部分手机没有语言助手导致程序崩溃的问题。如果遇到其他问题，还望大家指出，谢谢！

**Example:**

```xml
   <app.chen.com.MaterialSearchView
            android:id="@+id/search_view"
            style="@style/MaterialSearchViewStyle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
```

```
  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(true);

        //设置光标的颜色
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        //填充模拟数据
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        //文字变化的监听
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(findViewById(R.id.container), "Query: " + query, Snackbar.LENGTH_LONG)
                        .show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(VoiceActivity.this, newText, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //打开和关闭搜索布局的监听
        searchView.setSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShow() {
                Toast.makeText(VoiceActivity.this, "onSearchViewShow", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSearchViewClosed() {
                Toast.makeText(VoiceActivity.this, "onSearchViewClosed", Toast.LENGTH_SHORT).show();
            }
        });
		```



# License
	Copyright 2015 Miguel Catalan Bañuls

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
