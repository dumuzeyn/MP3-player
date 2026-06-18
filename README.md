# MP3 Player APK

<p align="center">
  <a href="https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk">
    <img src="https://img.shields.io/badge/Скачать_APK-MP3--Player.apk-black?style=for-the-badge" alt="Скачать APK">
  </a>
</p>

MP3 Player APK - нативное Android-приложение для локальной музыки. Оно работает без браузера, без WebView и без локального сервера: пользователь устанавливает APK, выбирает аудиофайлы через системный выбор файлов Android и слушает музыку прямо в приложении.

Приложение хранит только ссылки на выбранные пользователем аудиофайлы. Исходные песни не копируются, не изменяются и не удаляются с устройства. Если пользователь удалит или переместит файл вручную, сохраненная ссылка на него может перестать работать.

## Скачать

Готовый APK лежит в репозитории здесь:

```text
output/MP3-Player.apk
```

Прямая ссылка:

[Скачать MP3-Player.apk](https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk)

Если Android предупреждает об установке из неизвестного источника, нужно разрешить установку APK для приложения, через которое открыт файл. Это обычное поведение Android для приложений, установленных не из магазина.

## Что умеет приложение

- Добавлять одну или несколько песен через системный файловый менеджер Android.
- Показывать список песен с обложками и визуальной волновой формой.
- Читать название, исполнителя, альбом, жанр и обложку через `MediaMetadataRetriever`.
- Сортировать библиотеку: сначала английские названия, затем русские.
- Искать песни, избранное и плейлисты по совпадению в названии.
- Проигрывать одну песню, весь список подряд или список в случайном порядке.
- Создавать постоянные плейлисты.
- Добавлять песни в избранное и убирать их повторным нажатием на сердце.
- Добавлять песни в существующий плейлист или создавать новый плейлист из меню песни.
- Удалять песню из приложения без удаления файла с устройства.
- Удалять плейлист с подтверждением.
- Открывать большой плеер с обложкой, перемоткой, таймером, лайком, повтором и очередью.
- Показывать мини-плеер снизу экрана во время воспроизведения.
- Работать в фоне через Android foreground service и системное уведомление.
- Переключать светлую и темную черно-белую тему.

## Как устроен интерфейс

Главный экран собирается в `MainActivity.java` полностью кодом, без XML-разметки. Такой подход выбран, потому что приложение небольшое, а интерфейс часто менялся: все состояния, кнопки, вкладки, модальные окна и строки списков находятся рядом в одном файле.

Верхняя часть экрана содержит название приложения, иконку и кнопку смены темы. Ниже находится горизонтальная прокручиваемая панель разделов. Разделы повторяются циклом, поэтому визуально вкладки можно прокручивать бесконечно.

Основные разделы:

- `Песни` - вся библиотека.
- `Избранное` - песни, отмеченные сердцем.
- `Плейлисты` - пользовательские списки песен.
- `Жанры` - группировка по жанру из метаданных.
- `Исполнители` - группировка по исполнителю.
- `Альбомы` - группировка по альбому.

Строка песни строится методом `songRow(...)`. В ней задаются обложка, название, волновая форма, кнопка свойств или лайка и кнопка play/pause. Если нужно изменить размер строки, отступы, радиус обложек или расположение кнопок, начинать лучше с этого метода.

Большой плеер открывается методом `openFullPlayer()`. В нем находятся обложка, название песни, позиция в очереди, кнопки таймера/лайка/повтора, ползунок перемотки и кнопки предыдущей, текущей и следующей песни.

Мини-плеер создается методом `buildMiniPlayer()` и обновляется методом `updateMini()`. Он закреплен снизу экрана и скрывается при открытии большого плеера.

## Как работает воспроизведение

Воспроизведение вынесено в `PlayerService.java`. Это Android foreground service: когда музыка играет, Android видит постоянное уведомление с управлением. Благодаря этому трек продолжает играть после сворачивания приложения, блокировки экрана или обычного свайпа приложения из списка недавних.

Важно: если пользователь откроет системные настройки Android и нажмет принудительную остановку приложения, Android завершит сервис. Обычное приложение не может обходить это системное ограничение.

`MainActivity` отправляет команды сервису через `Intent`:

- `PLAY_INDEX` - запустить песню по индексу.
- `TOGGLE` - пауза или продолжение.
- `NEXT` - следующая песня.
- `PREV` - предыдущая песня.
- `STOP` - остановка.
- `SEEK` - перемотка.
- `LOOP` - изменение режима повтора.

Сервис хранит последние значения позиции, длительности, индекса и состояния воспроизведения в статических полях. Активность читает эти значения, чтобы обновлять интерфейс без сложной привязки к сервису.

## Очередь и режимы запуска

Когда пользователь нажимает play у отдельной песни, приложение запускает одиночное воспроизведение. После завершения этой песни очередь не продолжается автоматически, если пользователь не включил повтор.

Когда пользователь нажимает `Все подряд`, очередь формируется из текущего раздела в текущем порядке. В большом плеере позиция отображается относительно этой очереди, а не всей библиотеки.

Когда пользователь нажимает `Случайно`, приложение сначала берет все песни текущего раздела, перемешивает их и запускает получившуюся очередь. Это не одна случайная песня, а весь список в случайном порядке.

Повтор имеет три состояния:

1. Повтор выключен.
2. Повтор одной песни.
3. Повтор текущего списка.

## Где хранятся данные

Список выбранных треков хранится в `SharedPreferences`. За это отвечает `TrackStore.java`. Там же происходит чтение метаданных через `MediaMetadataRetriever`.

В `MainActivity.java` дополнительно сохраняются:

- избранные песни;
- плейлисты;
- выбранная тема;
- пользовательское значение таймера.

Данные плейлистов и избранного хранятся как JSON-строки в `SharedPreferences`. Это простое решение без базы данных, потому что объем данных небольшой: названия списков и URI выбранных файлов.

## Важные файлы

| Файл | За что отвечает |
|---|---|
| `app/src/main/AndroidManifest.xml` | Package name, разрешения, регистрация Activity и сервиса |
| `app/src/main/java/com/dumuzeyn/mp3player/MainActivity.java` | Весь интерфейс, списки, вкладки, модальные окна, темы, плейлисты |
| `app/src/main/java/com/dumuzeyn/mp3player/PlayerService.java` | Фоновое воспроизведение, MediaPlayer, уведомление, media session |
| `app/src/main/java/com/dumuzeyn/mp3player/Track.java` | Модель одной песни |
| `app/src/main/java/com/dumuzeyn/mp3player/TrackStore.java` | Сохранение библиотеки и чтение метаданных |
| `app/src/main/java/com/dumuzeyn/mp3player/WaveformView.java` | Рисунок волновой формы под названием песни |
| `app/src/main/res/drawable/ic_launcher.xml` | Иконка приложения на телефоне |
| `app/src/main/res/drawable/ic_music_vector.xml` | Иконка в шапке приложения |
| `app/src/main/res/values/strings.xml` | Название приложения |
| `app/src/main/res/values/styles.xml` | Базовая Android-тема |
| `build-apk.ps1` | Автоматическая сборка APK без Android Studio |
| `output/MP3-Player.apk` | Готовый установочный файл |

## Как поменять название приложения

Название на рабочем столе Android находится в:

```text
app/src/main/res/values/strings.xml
```

Нужно изменить значение `app_name`:

```xml
<string name="app_name">MP3 Player</string>
```

После изменения запустите сборку заново:

```powershell
.\build-apk.ps1
```

## Как поменять package name

Package name указан в двух местах:

```text
app/src/main/AndroidManifest.xml
app/src/main/java/com/dumuzeyn/mp3player/*.java
```

Если менять package name, нужно:

1. Изменить `package="..."` в `AndroidManifest.xml`.
2. Изменить строку `package ...;` в каждом Java-файле.
3. Переименовать папки внутри `app/src/main/java` под новый package.
4. Изменить action-строки в `PlayerService.java`, чтобы они соответствовали новому package.
5. Пересобрать APK.

## Как поменять дизайн

Основные цвета рассчитываются в `MainActivity.java` в методе `applyTheme()`. Сейчас интерфейс построен на двух режимах: светлом и темном. Цвета записываются в поля `bg`, `fg`, `panel`, `muted`, `line`.

Если нужно изменить внешний вид:

- `applyTheme()` - цвета темы.
- `buildHeader()` - верхняя строка с иконкой, названием и переключателем темы.
- `buildTabs()` и `styleTab(...)` - горизонтальное меню разделов.
- `songRow(...)` - карточка песни.
- `renderPlaylists()` - карточки плейлистов.
- `renderGroups(...)` - карточки жанров, исполнителей и альбомов.
- `openFullPlayer()` - большой плеер.
- `buildMiniPlayer()` и `updateMini()` - нижний мини-плеер.
- `coverView()` - скругление обложек.
- `button(...)`, `icon(...)`, `shuffleButton()`, `searchButton()` - общий стиль кнопок.

## Как поменять иконку

Иконка на рабочем столе Android:

```text
app/src/main/res/drawable/ic_launcher.xml
```

Иконка в шапке приложения:

```text
app/src/main/res/drawable/ic_music_vector.xml
```

Обе иконки сделаны как vector drawable. Если заменить их на другие vector drawable, приложение продолжит собираться без дополнительных изображений. Иконка в шапке красится цветом темы через `setColorFilter`, поэтому она меняется вместе со светлой и темной темой.

## Как поменять волновую форму

Волновая форма рисуется в `WaveformView.java`. Она не анализирует реальный звук, а строит стабильный рисунок на основе строки-ключа песни. Такой подход выбран специально: он не тратит ресурсы телефона на анализ аудио и при этом дает каждой песне отличающийся визуальный рисунок.

Если нужна другая форма:

- меняйте `onDraw(...)` в `WaveformView.java`;
- меняйте количество полос через цикл;
- меняйте высоту через формулу `heightFactor`;
- меняйте цвет в месте создания `WaveformView` в `MainActivity.wave(...)`.

## Как изменить работу плейлистов

Плейлисты хранятся в классе `Playlist` внутри `MainActivity.java`. Список плейлистов находится в поле `playlists`. Сохранение и загрузка выполняются методами `savePlaylists()` и `loadPlaylists()`.

Главные методы:

- `renderPlaylists()` - отображает все плейлисты.
- `openPlaylist(...)` - открывает конкретный плейлист.
- `openPlaylistPicker(...)` - выбор плейлиста при добавлении песни.
- `openPickSongsPanel(...)` - окно выбора нескольких песен.
- `confirmDeletePlaylist(...)` - подтверждение удаления плейлиста.

## Как собрать APK

На Windows откройте PowerShell в корне проекта и выполните:

```powershell
.\build-apk.ps1
```

Скрипт делает все сам:

1. Проверяет локальный Android SDK в `.android-sdk`.
2. При первом запуске скачивает Android command line tools.
3. Устанавливает `platforms;android-35`, `build-tools;35.0.0` и `platform-tools`.
4. Компилирует ресурсы через `aapt2`.
5. Линкует APK.
6. Компилирует Java через `javac`.
7. Упаковывает `.class` в `classes.jar`.
8. Создает `classes.dex` через `d8`.
9. Вставляет dex в APK.
10. Создает локальный ключ подписи, если его еще нет.
11. Выравнивает APK через `zipalign`.
12. Подписывает APK через `apksigner`.
13. Проверяет подпись.

После успешной сборки файл появится здесь:

```text
output/MP3-Player.apk
```

Папки `.android-sdk` и `build` являются рабочими каталогами сборки. Их не нужно загружать в репозиторий.

## Как установить через USB

Если на компьютере установлен ADB или уже скачан SDK этим проектом, можно установить APK так:

```powershell
.\.android-sdk\platform-tools\adb.exe install -r output\MP3-Player.apk
```

На телефоне должна быть включена отладка по USB, а компьютер должен быть разрешен в диалоге Android.

## Структура проекта

```text
Apk/
|-- app/
|   `-- src/
|       `-- main/
|           |-- AndroidManifest.xml
|           |-- java/
|           |   `-- com/
|           |       `-- dumuzeyn/
|           |           `-- mp3player/
|           |               |-- MainActivity.java
|           |               |-- PlayerService.java
|           |               |-- Track.java
|           |               |-- TrackStore.java
|           |               `-- WaveformView.java
|           `-- res/
|               |-- drawable/
|               |   |-- ic_launcher.xml
|               |   `-- ic_music_vector.xml
|               `-- values/
|                   |-- strings.xml
|                   `-- styles.xml
|-- output/
|   `-- MP3-Player.apk
|-- build-apk.ps1
|-- LICENSE
`-- README.md
```

## Лицензия

Проект разрешен для личного, учебного и некоммерческого использования. Коммерческая продажа, коммерческое распространение, перепродажа или включение в коммерческий продукт без отдельного разрешения запрещены. Подробности находятся в `LICENSE`.

---

# MP3 Player APK

<p align="center">
  <a href="https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk">
    <img src="https://img.shields.io/badge/Download_APK-MP3--Player.apk-black?style=for-the-badge" alt="Download APK">
  </a>
</p>

MP3 Player APK is a native Android application for local music playback. It does not use a browser, WebView, or a local web server. The user installs the APK, chooses audio files through the Android system file picker, and plays music directly inside the app.

The app stores only references to the audio files selected by the user. Original music files are not copied, modified, or deleted. If a file is deleted or moved manually, the saved reference may stop working.

## Download

The ready-to-install APK is stored here:

```text
output/MP3-Player.apk
```

Direct link:

[Download MP3-Player.apk](https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk)

If Android warns about installing from an unknown source, allow APK installation for the app that opened the file. This is normal Android behavior for apps installed outside an app store.

## Features

- Add one or multiple songs through the Android system file picker.
- Display songs with covers and waveform visuals.
- Read title, artist, album, genre, and embedded cover with `MediaMetadataRetriever`.
- Sort the library with English titles first and Russian titles after that.
- Search songs, favorites, and playlists by title match.
- Play one song, the whole list in order, or the current list in shuffled order.
- Create persistent playlists.
- Add songs to favorites and remove them by tapping the filled heart again.
- Add songs to an existing playlist or create a new playlist from the song menu.
- Remove a song from the app without deleting the file from the device.
- Delete playlists with confirmation.
- Open a full player with cover art, seek bar, timer, like, repeat, and queue controls.
- Show a fixed mini-player at the bottom while music is active.
- Continue playback in the background through an Android foreground service.
- Switch between light and dark black-and-white themes.

## Interface structure

The main screen is built in `MainActivity.java` entirely from Java code, without XML layouts. This was chosen because the app is compact and the interface changed often: screens, buttons, tabs, panels, and list rows are kept close together in one place.

The top area contains the app icon, app name, and theme switch. Below it is a horizontally scrollable tab bar. Tabs are repeated in cycles, so the section list feels endless while scrolling.

Main sections:

- `Songs` - the full library.
- `Favorites` - songs marked with a heart.
- `Playlists` - user-created song lists.
- `Genres` - grouped by genre metadata.
- `Artists` - grouped by artist metadata.
- `Albums` - grouped by album metadata.

A song row is built by `songRow(...)`. It defines the cover, title, waveform, song action or favorite button, and play/pause button. To change row height, spacing, cover radius, or button placement, start with this method.

The full player is opened by `openFullPlayer()`. It contains the cover, song title, queue position, timer/like/repeat buttons, seek bar, and previous/current/next controls.

The mini-player is created by `buildMiniPlayer()` and updated by `updateMini()`. It is fixed at the bottom of the screen and hidden while the full player is open.

## Playback behavior

Playback is handled by `PlayerService.java`. It is an Android foreground service: while music is playing, Android shows a persistent notification with playback controls. Because of this, the current track continues after the app is minimized, the screen is locked, or the app is normally swiped away from recent apps.

Important: if the user opens Android settings and presses Force stop, Android terminates the service. A normal Android app cannot bypass this system restriction.

`MainActivity` sends commands to the service through `Intent` actions:

- `PLAY_INDEX` - play a song by index.
- `TOGGLE` - pause or resume.
- `NEXT` - next song.
- `PREV` - previous song.
- `STOP` - stop playback.
- `SEEK` - seek to a position.
- `LOOP` - change repeat mode.

The service stores the latest playback index, duration, position, and state in static fields. The activity reads these values to refresh the interface without a heavy service binding layer.

## Queue and play modes

When the user taps play on a single song, the app starts one-shot playback. After that song ends, the queue does not continue automatically unless repeat is enabled.

When the user taps `Play all`, the queue is built from the current section in the current order. The full player shows the position inside this queue, not inside the entire library.

When the user taps `Shuffle`, the app takes all songs from the current section, shuffles them, and starts the resulting queue. It is not one random song; it is the whole current list in random order.

Repeat has three states:

1. Repeat off.
2. Repeat one song.
3. Repeat current list.

## Stored data

The selected track list is stored in `SharedPreferences`. `TrackStore.java` handles this and also reads metadata through `MediaMetadataRetriever`.

`MainActivity.java` additionally saves:

- favorite songs;
- playlists;
- selected theme;
- custom timer value.

Favorites and playlists are stored as JSON strings in `SharedPreferences`. This avoids a database because the stored data is small: playlist names and selected file URIs.

## Important files

| File | Responsibility |
|---|---|
| `app/src/main/AndroidManifest.xml` | Package name, permissions, Activity and service registration |
| `app/src/main/java/com/dumuzeyn/mp3player/MainActivity.java` | Entire interface, lists, tabs, panels, themes, playlists |
| `app/src/main/java/com/dumuzeyn/mp3player/PlayerService.java` | Background playback, MediaPlayer, notification, media session |
| `app/src/main/java/com/dumuzeyn/mp3player/Track.java` | Single-song data model |
| `app/src/main/java/com/dumuzeyn/mp3player/TrackStore.java` | Library persistence and metadata reading |
| `app/src/main/java/com/dumuzeyn/mp3player/WaveformView.java` | Waveform visual under a song title |
| `app/src/main/res/drawable/ic_launcher.xml` | Android launcher icon |
| `app/src/main/res/drawable/ic_music_vector.xml` | Header icon inside the app |
| `app/src/main/res/values/strings.xml` | App name |
| `app/src/main/res/values/styles.xml` | Base Android theme |
| `build-apk.ps1` | Automated APK build without Android Studio |
| `output/MP3-Player.apk` | Ready-to-install APK |

## Changing the app name

The launcher name is stored in:

```text
app/src/main/res/values/strings.xml
```

Change `app_name`:

```xml
<string name="app_name">MP3 Player</string>
```

Then rebuild:

```powershell
.\build-apk.ps1
```

## Changing the package name

The package name is used in two places:

```text
app/src/main/AndroidManifest.xml
app/src/main/java/com/dumuzeyn/mp3player/*.java
```

To change it:

1. Update `package="..."` in `AndroidManifest.xml`.
2. Update the `package ...;` line in every Java file.
3. Rename the folders under `app/src/main/java` to match the new package.
4. Update action strings in `PlayerService.java` so they match the new package.
5. Rebuild the APK.

## Changing the design

Theme colors are calculated in `MainActivity.java` inside `applyTheme()`. The current interface uses two black-and-white modes. Colors are stored in `bg`, `fg`, `panel`, `muted`, and `line`.

Useful places for design changes:

- `applyTheme()` - theme colors.
- `buildHeader()` - top row with icon, app name, and theme switch.
- `buildTabs()` and `styleTab(...)` - horizontal section menu.
- `songRow(...)` - song card.
- `renderPlaylists()` - playlist cards.
- `renderGroups(...)` - genre, artist, and album cards.
- `openFullPlayer()` - full player.
- `buildMiniPlayer()` and `updateMini()` - bottom mini-player.
- `coverView()` - cover corner radius.
- `button(...)`, `icon(...)`, `shuffleButton()`, `searchButton()` - shared button style.

## Changing the icon

Android launcher icon:

```text
app/src/main/res/drawable/ic_launcher.xml
```

Header icon inside the app:

```text
app/src/main/res/drawable/ic_music_vector.xml
```

Both icons are vector drawables. Replacing them with other vector drawables keeps the project free from raster image dependencies. The header icon is tinted with the current theme color through `setColorFilter`, so it switches together with the light and dark theme.

## Changing the waveform

The waveform is drawn in `WaveformView.java`. It does not analyze the real audio stream; it creates a stable visual pattern from the song key. This was chosen intentionally to avoid expensive audio analysis while still giving songs visually different rows.

To change it:

- edit `onDraw(...)` in `WaveformView.java`;
- change the number of bars in the loop;
- change height through the `heightFactor` formula;
- change color where `WaveformView` is created in `MainActivity.wave(...)`.

## Changing playlists

Playlists are represented by the `Playlist` class inside `MainActivity.java`. The playlist collection is stored in the `playlists` field. Saving and loading happen in `savePlaylists()` and `loadPlaylists()`.

Main methods:

- `renderPlaylists()` - displays all playlists.
- `openPlaylist(...)` - opens a single playlist.
- `openPlaylistPicker(...)` - selects a playlist while adding a song.
- `openPickSongsPanel(...)` - selects several songs.
- `confirmDeletePlaylist(...)` - confirms playlist deletion.

## Building the APK

On Windows, open PowerShell in the project root and run:

```powershell
.\build-apk.ps1
```

The script automatically:

1. Checks for a local Android SDK in `.android-sdk`.
2. Downloads Android command line tools on the first run.
3. Installs `platforms;android-35`, `build-tools;35.0.0`, and `platform-tools`.
4. Compiles resources with `aapt2`.
5. Links the APK.
6. Compiles Java with `javac`.
7. Packs `.class` files into `classes.jar`.
8. Creates `classes.dex` with `d8`.
9. Adds dex to the APK.
10. Creates a local signing key if it does not exist.
11. Aligns the APK with `zipalign`.
12. Signs the APK with `apksigner`.
13. Verifies the signature.

After a successful build, the file appears here:

```text
output/MP3-Player.apk
```

The `.android-sdk` and `build` folders are temporary build folders and should not be committed.

## Installing through USB

If ADB is available, install the APK with:

```powershell
.\.android-sdk\platform-tools\adb.exe install -r output\MP3-Player.apk
```

USB developer access must be enabled on the phone, and the computer must be allowed in the Android authorization dialog.

## Project structure

```text
Apk/
|-- app/
|   `-- src/
|       `-- main/
|           |-- AndroidManifest.xml
|           |-- java/
|           |   `-- com/
|           |       `-- dumuzeyn/
|           |           `-- mp3player/
|           |               |-- MainActivity.java
|           |               |-- PlayerService.java
|           |               |-- Track.java
|           |               |-- TrackStore.java
|           |               `-- WaveformView.java
|           `-- res/
|               |-- drawable/
|               |   |-- ic_launcher.xml
|               |   `-- ic_music_vector.xml
|               `-- values/
|                   |-- strings.xml
|                   `-- styles.xml
|-- output/
|   `-- MP3-Player.apk
|-- build-apk.ps1
|-- LICENSE
`-- README.md
```

## License

The project is free for personal, educational, and non-commercial use. Commercial sale, commercial redistribution, resale, or inclusion in a commercial product requires separate permission. See `LICENSE` for details.
