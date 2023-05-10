# Техническое задание

![Задание](image/task.png)

# Реализация

![Интерфейс](image/interface.png)

Главный вид представляет из себя список элементов (папки и файлы). Каждый элемент списка содержит в себе следующую информацию: 
1) название элемента
2) размер файла / количество файлов внутри
3) дата последнего изменения / дата создания файла
4) иконка элемента

### По поводу п.3: 
До api 26 информация о создании файла является не доступной / не хранится в системе. Поэтому для ранних версий используется информация о времени крайнего изменения элемента. В более новых версиях информация о создании файла уже является доступной

### По поводу п.4:
Приложение отображает следующие иконки:

![avi](app/src/main/res/drawable-nodpi/avi.png) avi - расширение

![bmp](app/src/main/res/drawable-nodpi/bmp.png) bmp - расширение

![doc](app/src/main/res/drawable-nodpi/doc.png) doc - расширение

![jpg](app/src/main/res/drawable-nodpi/jpg.png) jpg, jpeg - расширение

![mp3](app/src/main/res/drawable-nodpi/mp3.png) mp3 - расширение

![mp4](app/src/main/res/drawable-nodpi/mp4.png) mp4 - расширение

![pdf](app/src/main/res/drawable-nodpi/pdf.png) pdf - расширение

![png](app/src/main/res/drawable-nodpi/png.png) png - расширение

![txt](app/src/main/res/drawable-nodpi/txt.png) txt - расширение

![wav](app/src/main/res/drawable-nodpi/wav.png) wav - расширение

Также реализованы иконки под остальные файловые расширения и для директорий

Выбор сортировки реализован в скрытом меню, которое открывается при нажатии на кнопки в верхнем левом углу экрана или же свайпом вправо

![menu](image/menu.png)

При смене режима сортировки порядок файлов происходит сразу же


При каждом запуске приложения происходит заполнение базы данных в виде сущности, состоящей из абсолютного пути файла и его хэш-значения по кодировке SHA256. При каждом новом запуске приложения формируется список файлом, хэш - значения которых были изменены. Эти файлы можно увидеть по нажатии на иконку справа вверху. При повторном нажатии на иконку отображается список файлов по указанному пути.

## Реализация поиска

### Содержание файла
![file](image/1file.jpg)
### Размер файла
![file](image/1size.jpg)
### Отображение файлов, которые были изменены
![file](image/1find.jpg)
### Содержание файла при следующем запуске приложения
![file](image/2file.jpg)
### Новый размер файла
![file](image/2size.jpg)
### Отображение файлов, которые были изменены
![file](image/2find.jpg)

## Дополнительные моменты:

Приложение запрашивает доступ к файлам телефона. Также в новых системах с api более 29 запрашивается доступ ко ВСЕМ файлам в телефоне

Системной клавише назад и TextView с отображением абсолютного пути присвоены команды перехода на уровень вверх до корневого элемента

### Задачи из задания реализованы, кроме последнего пункта с "быстрой" работой со списком (задача носталась не ясна)