-- V20: 本地化演示数据中的用户可见英文标题和机构名。

UPDATE course SET
    provider = '甲骨文 Java 教程',
    resource_title = 'Java 官方教程：入门与语言基础'
WHERE course_code = 'JAVA-BASE';

UPDATE course SET
    provider = '谷歌开发者课程',
    resource_title = '机器学习速成课程'
WHERE course_code = 'AI-INTRO';

UPDATE course SET
    provider = 'Python 官方教程',
    resource_title = 'Python 官方入门教程'
WHERE course_code = 'PYTHON-BASE';

UPDATE course SET
    provider = '麻省理工开放课程',
    resource_title = '计算机科学与 Python 编程导论'
WHERE course_code = 'PYTHON-CS-MIT';

UPDATE course SET
    provider = '动手学深度学习',
    resource_title = '动手学深度学习'
WHERE course_code = 'DL-OPEN';

UPDATE course SET
    provider = '开放经济学资源',
    resource_title = '微观经济学与开放经济学资源'
WHERE course_code = 'ECON-MICRO';

UPDATE course SET
    resource_title = 'Java 高级学习路径'
WHERE course_code = 'JAVA-ADV';

UPDATE course SET
    resource_title = '机器学习实战训练路径'
WHERE course_code = 'AI-PRACTICE';
