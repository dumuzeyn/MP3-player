# MP3 Player APK

<p align="center">
  <a href="https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk">
    <img src="https://img.shields.io/badge/Скачать_APK-MP3--Player.apk-black?style=for-the-badge" alt="Скачать APK">
  </a>
</p>

[English version](#engk)

MP3 Player APK — полноценное Android-приложение для локального прослушивания музыки. Оно не использует браузер, WebView или локальный сервер. Пользователь устанавливает APK, выбирает аудиофайлы через системное окно Android, а приложение сохраняет ссылки на выбранные файлы и воспроизводит их через Android `MediaPlayer` в foreground service.

Приложение не копирует, не изменяет и не удаляет исходные музыкальные файлы на телефоне. Если удалить песню из приложения, исчезает только запись внутри приложения. Сам файл остается на устройстве.

## Скачать

Готовый APK находится здесь:

```text
output/MP3-Player.apk
```

Если Android сообщает о конфликте пакета при тестировании, рядом лежит отдельная тестовая сборка:

```text
output/MP3-Player-test.apk
```

Прямая ссылка:

[Скачать MP3-Player.apk](https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk)

Если Android предупреждает об установке из неизвестного источника, нужно разрешить установку APK для приложения, через которое открыт файл. Это стандартное поведение Android для приложений не из магазина.

## Возможности

- Добавление одной или нескольких песен через системный выбор файлов Android.
- Отображение песен с обложками и волновой визуализацией.
- Чтение названия, исполнителя, альбома, жанра и встроенной обложки через `MediaMetadataRetriever`.
- Сортировка библиотеки: сначала английские названия, потом русские.
- Поиск песен, избранного и плейлистов по совпадению в названии.
- Проигрывание одной песни, всего текущего списка подряд или всего текущего списка в случайном порядке.
- Постоянные плейлисты, которые сохраняются между запусками.
- Добавление песен в избранное и удаление из избранного повторным нажатием на заполненное сердце.
- Добавление песни в существующий плейлист или создание нового плейлиста из меню песни.
- Удаление песни из приложения без удаления файла с телефона.
- Удаление песен из конкретного плейлиста.
- Удаление плейлистов с подтверждением.
- Большой плеер с обложкой, перемоткой, таймером, лайком, повтором и списком очереди.
- Мини-плеер снизу экрана во время активного воспроизведения.
- Восстановление мини-плеера после остановки песни и повторного открытия приложения. По умолчанию состояние хранится 2 часа, срок можно изменить в настройках.
- Фоновое воспроизведение через Android foreground service.
- Светлая и темная черно-белая тема.
- Переключение языка интерфейса между English и Русский через стилизованное окно приложения.
- Безопасный импорт аудио через системный выбор файлов Android с проверкой MIME-типа, расширения и размера файла.
- Устойчивое чтение метаданных и обложек: поврежденные или слишком тяжелые MP3 не должны закрывать приложение.

## Структура интерфейса

Верхняя часть содержит иконку приложения и надпись `MP3 Player`. Кнопка смены темы больше не находится рядом с названием приложения: она перенесена в раздел `Settings`, чтобы шапка не смешивала настройки и основные действия.

Ниже находится горизонтальное колесо меню. В нем есть разделы:

- `Songs` / `Песни`
- `Favorites` / `Избранное`
- `Playlists` / `Плейлисты`
- `Genres` / `Жанры`
- `Artists` / `Исполнители`
- `Albums` / `Альбомы`
- `Settings` / `Настройки`

Колесо повторяет вкладки несколько раз, поэтому оно ощущается бесконечным. Его можно крутить пальцем свободно. Само вращение колеса не переключает раздел, чтобы пользователь мог спокойно докрутить список до нужного пункта.

Раздел переключается только двумя способами:

1. Нажатием на название раздела в верхнем колесе.
2. Свайпом по основной области экрана, не по самому колесу.

## Как работает кольцо меню и свайпы

Логика находится в `MainActivity.java`.

`dispatchTouchEvent(...)` пропускает касания через `handlePageSwipe(...)`. Метод `isInsideTabs(...)` проверяет, начался ли жест внутри `HorizontalScrollView`, где находится колесо меню.

Если жест начался на колесе, он не считается свайпом страницы. В этом случае пользователь просто прокручивает колесо, а текущий раздел не меняется.

Если жест начался ниже, в основной области экрана, приложение считает это свайпом между разделами. Свайп в одну сторону открывает соседний раздел слева, свайп в другую — соседний раздел справа.

При переключении раздела вызывается `switchTabAnimated(...)`. Он одновременно:

- сдвигает текущий список в сторону;
- показывает новый список;
- докручивает колесо меню к активной вкладке.

Докрутка колеса выполняется не через долгий стандартный `smoothScrollTo(...)`, а через `ValueAnimator` в методе `animateTabsScrollTo(...)`. Это сделано, чтобы движение было быстрее и контролируемее.

`scrollTabsToActive(...)` ищет ближайшую копию нужной вкладки среди повторяющихся вкладок. Поэтому переход между крайними пунктами, например `Settings` и `Songs`, должен идти коротким путем, а не проворачивать весь список.

## Settings / Настройки

Раздел `Settings` отвечает за общие настройки приложения.

В нем находятся:

- переключение темы;
- выбор языка интерфейса: `English` или `Русский`;
- настройка времени, в течение которого мини-плеер помнит остановленную песню;
- ссылка на GitHub проекта;
- удаление всех песен из приложения с подтверждением;
- удаление всех плейлистов с подтверждением.

Удаление всех песен очищает список песен, избранное и ссылки на песни внутри плейлистов. Файлы на телефоне не удаляются.

Удаление всех плейлистов очищает только коллекцию плейлистов. Песни остаются в приложении.

Тексты в настройках выровнены по левому краю. Выбор языка и выбор времени памяти мини-плеера открываются в нижнем окне, которое использует те же цвета, панели и кнопки, что и остальное приложение.

## Переключение языка

Выбранный язык хранится в `SharedPreferences` под ключом `LANGUAGE`.

Основные подписи интерфейса выбираются через метод:

```java
tr(String en, String ru)
```

Если выбран английский язык, метод возвращает первую строку. Если русский — вторую.

Названия вкладок собираются в `refreshTabLabels()`. После смены языка приложение вызывает `buildUi()`, поэтому интерфейс сразу перестраивается с новыми названиями.

## Поведение воспроизведения

Если нажать play у отдельной песни, приложение запускает одиночное воспроизведение. После окончания этой песни очередь не продолжается автоматически, если не включен повтор.

Если нажать `Play all`, очередь строится из текущего раздела в текущем порядке. Большой плеер показывает позицию внутри этой очереди, а не внутри всей библиотеки.

Если нажать `Shuffle`, приложение берет все песни из текущего раздела, перемешивает их и запускает получившуюся очередь. Это не одна случайная песня, а весь текущий список в случайном порядке.

Повтор имеет три состояния:

1. Повтор выключен.
2. Повтор одной песни.
3. Повтор текущего списка.

Таймер сна останавливает воспроизведение после выбранного времени. Пользовательское время сохраняется и остается доступным при следующем открытии таймера.

Если пользователь остановил песню и закрыл приложение, мини-плеер может восстановиться при следующем запуске на той же позиции. По умолчанию это состояние действительно 2 часа. В настройках можно выбрать другой срок или отключить восстановление.

## Хранение данных

Список выбранных песен хранится в `SharedPreferences`. За это отвечает `TrackStore.java`. Метаданные читаются через `MediaMetadataRetriever`.

`MainActivity.java` дополнительно сохраняет:

- избранные песни;
- плейлисты;
- выбранную тему;
- выбранный язык;
- пользовательское время таймера;
- время хранения остановленного мини-плеера.

`PlayerService.java` сохраняет небольшой снимок последнего воспроизведения: URI песни, позицию, длительность, режим повтора и очередь. Снимок используется только локально, чтобы восстановить мини-плеер после повторного открытия приложения.

Избранное и плейлисты хранятся как JSON-строки в `SharedPreferences`. База данных не используется, потому что объем данных небольшой: названия плейлистов и URI выбранных файлов.

## Важные файлы

| Файл | За что отвечает |
|---|---|
| `app/src/main/AndroidManifest.xml` | Package name, разрешения, регистрация Activity и service |
| `app/src/main/java/com/dumuzeyn/mp3player/MainActivity.java` | Весь интерфейс, списки, вкладки, панели, темы, плейлисты, настройки |
| `app/src/main/java/com/dumuzeyn/mp3player/PlayerService.java` | Фоновое воспроизведение, `MediaPlayer`, уведомление, media session |
| `app/src/main/java/com/dumuzeyn/mp3player/Track.java` | Модель одной песни |
| `app/src/main/java/com/dumuzeyn/mp3player/TrackStore.java` | Сохранение библиотеки и чтение метаданных |
| `app/src/main/java/com/dumuzeyn/mp3player/WaveformView.java` | Волновая визуализация под названием песни |
| `app/src/main/res/drawable/ic_launcher.xml` | Иконка приложения на рабочем столе Android |
| `app/src/main/res/drawable/ic_music_vector.xml` | Иконка в шапке приложения |
| `app/src/main/res/values/strings.xml` | Название приложения |
| `app/src/main/res/values/styles.xml` | Базовая Android-тема |
| `build-apk.ps1` | Автоматическая сборка APK без Android Studio |
| `output/MP3-Player.apk` | Готовый APK для установки |

## Безопасность

Приложение использует системный выбор файлов Android и не запрашивает полный доступ ко всему хранилищу. При добавлении песни проверяются `content://` URI, MIME-тип, расширение и примерный размер файла. Приложение не выполняет данные из ID3-тегов, не открывает ссылки из метаданных, не использует WebView, не запускает локальный сервер и не включает HTTP-трафик.

Сервис воспроизведения не экспортируется наружу, исходные MP3-файлы не изменяются, резервное копирование данных приложения отключено через manifest, а чтение метаданных и обложек обернуто в защитную обработку ошибок. Слишком большие встроенные обложки пропускаются, чтобы не перегружать память.

## Что менять в коде

- `buildHeader()` — верхняя строка с иконкой и названием.
- `buildTabs()` — создание горизонтального кольца меню.
- `styleTab(...)` — внешний вид активной и неактивной вкладки.
- `handlePageSwipe(...)` — распознавание свайпов по основной области.
- `isInsideTabs(...)` — защита от переключения раздела при прокрутке колеса меню.
- `switchTabAnimated(...)` — анимация смены раздела.
- `scrollTabsToActive(...)` — выбор ближайшей копии нужной вкладки в кольце.
- `animateTabsScrollTo(...)` — короткая плавная докрутка колеса.
- `renderSettings(...)` — экран настроек.
- `addSettingsButton(...)` — стиль кнопок настроек.
- `songRow(...)` — строка песни.
- `renderPlaylists()` — карточки плейлистов.
- `renderGroups(...)` — разделы жанров, исполнителей и альбомов.
- `openFullPlayer()` — большой плеер.
- `buildMiniPlayer()` и `updateMini()` — мини-плеер.
- `coverView()` — отображение обложек.
- `button(...)`, `icon(...)`, `shuffleButton()`, `searchButton()` — общий стиль кнопок.

## Изменение названия приложения

Название для Android хранится здесь:

```text
app/src/main/res/values/strings.xml
```

Измените `app_name`:

```xml
<string name="app_name">MP3 Player</string>
```

После этого пересоберите APK:

```powershell
.\build-apk.ps1
```

## Подпись APK

Обычная локальная сборка использует debug-подпись:

```powershell
.\build-apk.ps1
```

Release-подпись не создается автоматически и не хранит пароль в репозитории. Для release-сборки нужно держать ключ вне проекта и передать параметры через переменные окружения:

```powershell
$env:MP3_SIGNING_MODE="release"
$env:MP3_RELEASE_KEYSTORE="D:\keys\mp3-player-release.jks"
$env:MP3_RELEASE_KEY_ALIAS="mp3player"
$env:MP3_RELEASE_STORE_PASS="длинный_пароль_хранилища"
$env:MP3_RELEASE_KEY_PASS="длинный_пароль_ключа"
.\build-apk.ps1
```

Если release-ключ не найден или переменные не заданы, сборка завершается ошибкой. Это защищает от случайной публикации APK, подписанного слабым автоматически созданным ключом.

## Изменение package name

Package name используется в двух местах:

```text
app/src/main/AndroidManifest.xml
app/src/main/java/com/dumuzeyn/mp3player/*.java
```

Чтобы изменить package name:

1. Измените `package="..."` в `AndroidManifest.xml`.
2. Измените строку `package ...;` во всех Java-файлах.
3. Переименуйте папки внутри `app/src/main/java` под новый package.
4. Обновите action-строки в `PlayerService.java`, чтобы они соответствовали новому package.
5. Пересоберите APK.

## Изменение дизайна

Цвета темы рассчитываются в `MainActivity.java` в методе `colors()`. Интерфейс использует два черно-белых режима. Основные цвета хранятся в переменных `bg`, `fg`, `panel`, `muted` и `line`.

Если нужно поменять внешний вид:

- меняйте цвета в `colors()`;
- меняйте форму карточек в `rounded(...)` и `setSurface(...)`;
- меняйте общие кнопки в `button(...)` и `icon(...)`;
- меняйте обложки в `coverView()`;
- меняйте визуализацию песен в `WaveformView.java`.

## Изменение иконки

Иконка Android launcher:

```text
app/src/main/res/drawable/ic_launcher.xml
```

Иконка внутри приложения:

```text
app/src/main/res/drawable/ic_music_vector.xml
```

Обе иконки являются vector drawable. Это удобно: они не размываются и не требуют набора PNG под разные размеры экрана. Иконка в шапке окрашивается через `setColorFilter`, поэтому меняет цвет вместе с темой.

## Изменение волновой визуализации

Волновая визуализация рисуется в `WaveformView.java`. Она не анализирует реальный аудиопоток, а создает стабильный рисунок на основе ключа песни. Так сделано специально, чтобы не тратить ресурсы на тяжелый анализ звука и при этом давать каждой песне отличающийся визуальный ряд.

Чтобы изменить визуализацию:

- редактируйте `onDraw(...)` в `WaveformView.java`;
- меняйте количество линий в цикле;
- меняйте высоту через формулу `heightFactor`;
- меняйте цвет в `MainActivity.wave(...)`.

## Плейлисты

Плейлисты представлены внутренним классом `Playlist` в `MainActivity.java`. Список плейлистов хранится в поле `playlists`.

Основные методы:

- `renderPlaylists()` — показывает все плейлисты.
- `openPlaylist(...)` — открывает конкретный плейлист.
- `choosePlaylistForTrack(...)` — выбирает плейлист при добавлении песни.
- `openAddToPlaylist(...)` — открывает окно выбора нескольких песен для плейлиста.
- `confirmDeletePlaylist(...)` — подтверждает удаление плейлиста.
- `confirmDeleteAllPlaylists()` — подтверждает удаление всех плейлистов.

## Сборка APK

В Windows откройте PowerShell в корне проекта и выполните:

```powershell
.\build-apk.ps1
```

Скрипт автоматически:

1. Проверяет локальный Android SDK в `.android-sdk`.
2. При первом запуске скачивает Android command line tools.
3. Устанавливает `platforms;android-35`, `build-tools;35.0.0` и `platform-tools`.
4. Компилирует ресурсы через `aapt2`.
5. Линкует APK.
6. Компилирует Java через `javac`.
7. Упаковывает `.class` файлы в `classes.jar`.
8. Создает `classes.dex` через `d8`.
9. Добавляет dex в APK.
10. Создает локальный ключ подписи, если его еще нет.
11. Выравнивает APK через `zipalign`.
12. Подписывает APK через `apksigner`.
13. Проверяет подпись.

После успешной сборки файл появляется здесь:

```text
output/MP3-Player.apk
```

Папки `.android-sdk` и `build` являются временными папками сборки и не должны попадать в репозиторий.

## Установка через USB

Если доступен ADB, установите APK так:

```powershell
.\.android-sdk\platform-tools\adb.exe install -r output\MP3-Player.apk
```

На телефоне должна быть включена отладка по USB, а компьютер должен быть разрешен в системном окне Android.

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

Проект бесплатен для личного, учебного и некоммерческого использования. Коммерческая продажа, коммерческое распространение, перепродажа или включение в коммерческий продукт требуют отдельного разрешения. Подробности находятся в `LICENSE`.

>**Автор проекта: Зейналов У.Р.о.**
---
<h1 id=engk>
 MP3 Player APK
 </h1>

<p align="center">
  <a href="https://github.com/dumuzeyn/MP3-player/raw/main/output/MP3-Player.apk">
    <img src="https://img.shields.io/badge/Download_APK-MP3--Player.apk-black?style=for-the-badge" alt="Download APK">
  </a>
</p>

MP3 Player APK is a native Android application for local music playback. It does not use a browser, WebView, or a local web server. The user installs the APK, chooses audio files through the Android system file picker, and plays music through Android `MediaPlayer` inside a foreground service.

The app stores only references to the audio files selected by the user. Original music files are not copied, modified, or deleted. If a song is removed from the app, only the app entry disappears. The original file remains on the device.

## Download

The ready-to-install APK is stored here:

```text
output/MP3-Player.apk
```

If Android reports a package conflict during testing, a separate test build is also available:

```text
output/MP3-Player-test.apk
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
- Play one song, the whole current list in order, or the whole current list in shuffled order.
- Create persistent playlists.
- Add songs to favorites and remove them by tapping the filled heart again.
- Add songs to an existing playlist or create a new playlist from the song menu.
- Remove a song from the app without deleting the file from the device.
- Remove songs from a specific playlist.
- Delete playlists with confirmation.
- Open a full player with cover art, seek bar, timer, like, repeat, and queue controls.
- Show a fixed mini-player at the bottom while music is active.
- Restore the mini-player after stopping a song and reopening the app. By default the state is kept for 2 hours, and the timeout can be changed in Settings.
- Continue playback in the background through an Android foreground service.
- Switch between light and dark black-and-white themes.
- Switch the interface language between English and Russian through a styled in-app panel.
- Import audio safely through Android's system file picker with MIME type, extension, and file size checks.
- Handle damaged or unusually heavy MP3 metadata and covers without closing the app.

## Interface Structure

The top area contains the app icon and the `MP3 Player` title. The theme switch is no longer near the app title. It was moved into `Settings` so the header stays cleaner.

Below the header there is a horizontal menu wheel with these sections:

- `Songs`
- `Favorites`
- `Playlists`
- `Genres`
- `Artists`
- `Albums`
- `Settings`

The wheel repeats tab names several times, so it feels endless. The user can scroll it freely with a finger. Scrolling the wheel does not switch the active section.

A section changes only in two cases:

1. The user taps a section name in the top wheel.
2. The user swipes across the main content area, outside the wheel.

## Menu Wheel And Swipe Behavior

The logic is in `MainActivity.java`.

`dispatchTouchEvent(...)` passes touch events through `handlePageSwipe(...)`. The method `isInsideTabs(...)` checks whether the gesture started inside the `HorizontalScrollView` that contains the menu wheel.

If the gesture started on the wheel, it is not treated as a page swipe. The wheel simply scrolls.

If the gesture started below the wheel, in the main content area, the app treats it as a section swipe and moves to the neighboring tab.

When a tab changes, `switchTabAnimated(...)` runs. It does three things:

- slides the current list out;
- shows the new list;
- moves the menu wheel to the active tab.

The wheel movement does not use the default long `smoothScrollTo(...)`. Instead, `animateTabsScrollTo(...)` uses `ValueAnimator`, which makes the movement shorter and more controlled.

`scrollTabsToActive(...)` searches for the closest repeated copy of the target tab in the wheel. This prevents transitions such as `Settings` to `Songs` from spinning through the whole list.

## Settings

The `Settings` section contains global app settings.

It includes:

- light/dark theme switching;
- interface language selection: `English` or `Русский`;
- mini-player memory timeout;
- GitHub project link;
- delete all songs from the app, with confirmation;
- delete all playlists, with confirmation.

Deleting all songs does not delete files from the phone. It clears only the app library, favorites, and song references inside playlists, then saves the new state through `TrackStore.save(...)` and `saveState()`.

Deleting all playlists clears only the `playlists` collection. Songs remain in the app.

All Settings text is aligned to the left. Language selection and mini-player memory selection open styled bottom panels that reuse the app's own colors, panels, and buttons.

## Language Switching

The selected language is stored in `SharedPreferences` under the `LANGUAGE` key.

Main interface labels use:

```java
tr(String en, String ru)
```

If English is selected, the method returns the first string. If Russian is selected, it returns the second string.

Tab labels are rebuilt in `refreshTabLabels()`. After the language changes, the interface is rebuilt through `buildUi()`, so tab names update immediately.

## Playback Behavior

When the user taps play on a single song, the app starts one-shot playback. After that song ends, the queue does not continue automatically unless repeat is enabled.

When the user taps `Play all`, the queue is built from the current section in the current order. The full player shows the position inside this queue, not inside the entire library.

When the user taps `Shuffle`, the app takes all songs from the current section, shuffles them, and starts the resulting queue. It is not one random song; it is the whole current list in random order.

Repeat has three states:

1. Repeat off.
2. Repeat one song.
3. Repeat current list.

The sleep timer stops playback after the selected time. The custom time value is saved and remains available the next time the timer is opened.

If the user stops a song and closes the app, the mini-player can be restored on the next launch at the same position. By default this state is valid for 2 hours. Settings allow choosing another timeout or turning this behavior off.

## Stored Data

The selected track list is stored in `SharedPreferences`. `TrackStore.java` handles this and also reads metadata through `MediaMetadataRetriever`.

`MainActivity.java` additionally saves:

- favorite songs;
- playlists;
- selected theme;
- selected language;
- custom timer value;
- mini-player memory timeout.

`PlayerService.java` stores a small local playback snapshot: song URI, position, duration, repeat mode, and queue. This snapshot is only used locally to restore the mini-player after reopening the app.

Favorites and playlists are stored as JSON strings in `SharedPreferences`. This avoids a database because the stored data is small: playlist names and selected file URIs.

## Important Files

| File | Responsibility |
|---|---|
| `app/src/main/AndroidManifest.xml` | Package name, permissions, Activity and service registration |
| `app/src/main/java/com/dumuzeyn/mp3player/MainActivity.java` | Entire interface, lists, tabs, panels, themes, playlists, settings |
| `app/src/main/java/com/dumuzeyn/mp3player/PlayerService.java` | Background playback, `MediaPlayer`, notification, media session |
| `app/src/main/java/com/dumuzeyn/mp3player/Track.java` | Single-song data model |
| `app/src/main/java/com/dumuzeyn/mp3player/TrackStore.java` | Library persistence and metadata reading |
| `app/src/main/java/com/dumuzeyn/mp3player/WaveformView.java` | Waveform visual under a song title |
| `app/src/main/res/drawable/ic_launcher.xml` | Android launcher icon |
| `app/src/main/res/drawable/ic_music_vector.xml` | Header icon inside the app |
| `app/src/main/res/values/strings.xml` | App name |
| `app/src/main/res/values/styles.xml` | Base Android theme |
| `build-apk.ps1` | Automated APK build without Android Studio |
| `output/MP3-Player.apk` | Ready-to-install APK |

## Security

The app uses Android's system file picker and does not request full access to device storage. When adding a song, it checks `content://` URIs, MIME type, extension, and approximate file size. It does not execute ID3 metadata, does not open links from metadata, does not use WebView, does not run a local server, and disables cleartext HTTP traffic.

The playback service is not exported, original MP3 files are not modified, app data backup is disabled in the manifest, and metadata/cover reading is wrapped in defensive error handling. Oversized embedded covers are skipped to avoid memory pressure.

## Where To Change The Code

- `buildHeader()` - top row with icon and app name.
- `buildTabs()` - creates the horizontal menu wheel.
- `styleTab(...)` - active and inactive tab appearance.
- `handlePageSwipe(...)` - detects swipes across the main content area.
- `isInsideTabs(...)` - prevents section switching while the user scrolls the menu wheel.
- `switchTabAnimated(...)` - animated section transition.
- `scrollTabsToActive(...)` - selects the nearest repeated copy of the target tab.
- `animateTabsScrollTo(...)` - short smooth wheel movement.
- `renderSettings(...)` - Settings screen.
- `addSettingsButton(...)` - Settings button style.
- `songRow(...)` - song row.
- `renderPlaylists()` - playlist cards.
- `renderGroups(...)` - genre, artist, and album sections.
- `openFullPlayer()` - full player.
- `buildMiniPlayer()` and `updateMini()` - bottom mini-player.
- `coverView()` - cover display.
- `button(...)`, `icon(...)`, `shuffleButton()`, `searchButton()` - shared button style.

## Changing The App Name

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

## Changing The Package Name

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

## Changing The Design

Theme colors are calculated in `MainActivity.java` inside `colors()`. The current interface uses two black-and-white modes. Colors are stored in `bg`, `fg`, `panel`, `muted`, and `line`.

Useful places for design changes:

- `colors()` - theme colors.
- `rounded(...)` and `setSurface(...)` - card and panel shape.
- `button(...)` and `icon(...)` - shared button style.
- `coverView()` - cover corner radius.
- `WaveformView.java` - song visualizer.

## Changing The Icon

Android launcher icon:

```text
app/src/main/res/drawable/ic_launcher.xml
```

Header icon inside the app:

```text
app/src/main/res/drawable/ic_music_vector.xml
```

Both icons are vector drawables. Replacing them with other vector drawables keeps the project free from raster image dependencies. The header icon is tinted with the current theme color through `setColorFilter`, so it switches together with the light and dark theme.

## Changing The Waveform

The waveform is drawn in `WaveformView.java`. It does not analyze the real audio stream; it creates a stable visual pattern from the song key. This avoids expensive audio analysis while still giving songs visually different rows.

To change it:

- edit `onDraw(...)` in `WaveformView.java`;
- change the number of bars in the loop;
- change height through the `heightFactor` formula;
- change color where `WaveformView` is created in `MainActivity.wave(...)`.

## Playlists

Playlists are represented by the `Playlist` class inside `MainActivity.java`. The playlist collection is stored in the `playlists` field.

Main methods:

- `renderPlaylists()` - displays all playlists.
- `openPlaylist(...)` - opens a single playlist.
- `choosePlaylistForTrack(...)` - selects a playlist while adding a song.
- `openAddToPlaylist(...)` - opens song selection for a playlist.
- `confirmDeletePlaylist(...)` - confirms playlist deletion.
- `confirmDeleteAllPlaylists()` - confirms deletion of all playlists.

## Building The APK

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

## Installing Through USB

If ADB is available, install the APK with:

```powershell
.\.android-sdk\platform-tools\adb.exe install -r output\MP3-Player.apk
```

USB developer access must be enabled on the phone, and the computer must be allowed in the Android authorization dialog.

## Project Structure

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

> ** Author of project: Zeynalov U.R.o.**
