-- ============================================================
-- V11: 课程学习资源与阶段
-- ============================================================

ALTER TABLE course
    ADD COLUMN resource_title VARCHAR(160),
    ADD COLUMN resource_url VARCHAR(512),
    ADD COLUMN resource_summary VARCHAR(512),
    ADD COLUMN estimated_minutes INT DEFAULT 90,
    ADD COLUMN learning_stages TEXT;

UPDATE course SET
    provider = 'Oracle Java Tutorials',
    resource_title = 'The Java Tutorials - Getting Started / Learning the Java Language',
    resource_url = 'https://docs.oracle.com/javase/tutorial/',
    resource_summary = 'Oracle 官方 Java 教程，覆盖环境准备、基础语法、类与对象、集合、异常和常用 API。',
    estimated_minutes = 180,
    learning_stages = '[{"title":"环境与第一个程序","description":"阅读 Getting Started，完成 JDK/IDE 准备并运行 Hello World。"},{"title":"语法与面向对象","description":"学习变量、控制流、类、对象、继承和接口。"},{"title":"核心类库实践","description":"完成集合、异常、输入输出等章节的例题练习。"}]'
WHERE course_code = 'JAVA-BASE';

UPDATE course SET
    provider = 'Google for Developers',
    resource_title = 'Machine Learning Crash Course',
    resource_url = 'https://developers.google.com/machine-learning/crash-course',
    resource_summary = 'Google 面向初学者的机器学习速成课程，包含视频、交互可视化和动手练习。',
    estimated_minutes = 240,
    learning_stages = '[{"title":"机器学习概念","description":"学习监督学习、模型、损失函数和训练/评估流程。"},{"title":"回归与分类","description":"完成线性回归、逻辑回归和分类指标模块。"},{"title":"数据与真实场景","description":"学习数值/类别特征、过拟合、生产系统和公平性基础。"}]'
WHERE course_code = 'AI-INTRO';

INSERT INTO course (course_code, course_name, provider, category, credit_value, credit_point, status, resource_title, resource_url, resource_summary, estimated_minutes, learning_stages, created_at, updated_at, deleted, institution_id)
VALUES
('PYTHON-BASE', 'Python编程入门', 'Python Software Foundation', '编程开发', 2.50, 25, 'PUBLISHED',
 'The Python Tutorial', 'https://docs.python.org/3/tutorial/',
 'Python 官方教程，适合有一点编程基础的学习者系统了解 Python 语法和标准库。',
 180,
 '[{"title":"解释器与基础语法","description":"学习解释器使用、数字、字符串、列表和第一个 Python 程序。"},{"title":"控制流与函数","description":"掌握 if、for、函数定义、参数和模块。"},{"title":"数据结构与文件","description":"学习列表/字典/集合、异常、文件读写和标准库入门。"}]',
 NOW(), NOW(), 0, 1),
('PYTHON-CS-MIT', '计算机科学与Python实践', 'MIT OpenCourseWare', '编程开发', 3.00, 30, 'PUBLISHED',
 'Introduction to Computer Science and Programming in Python', 'https://ocw.mit.edu/courses/6-0001-introduction-to-computer-science-and-programming-in-python-fall-2016/',
 'MIT OCW 计算机科学入门课程，使用 Python 讲授计算思维、算法和问题求解。',
 300,
 '[{"title":"计算思维入门","description":"了解程序、数据类型、分支、迭代与基础算法思维。"},{"title":"函数与抽象","description":"学习函数分解、作用域、递归和测试。"},{"title":"复杂度与实践","description":"理解搜索、排序、复杂度，并完成课程练习。"}]',
 NOW(), NOW(), 0, 1),
('DL-OPEN', '深度学习开放实践', 'Dive into Deep Learning', '人工智能', 3.50, 35, 'PUBLISHED',
 'Dive into Deep Learning', 'https://d2l.ai/',
 '开源深度学习教材，使用可运行代码讲解深度学习概念、模型和实践。',
 360,
 '[{"title":"预备知识","description":"学习张量、数据处理、线性代数和自动微分基础。"},{"title":"神经网络基础","description":"完成线性回归、多层感知机和模型训练章节。"},{"title":"深度模型实践","description":"继续学习卷积神经网络、循环网络或注意力模型，并运行示例代码。"}]',
 NOW(), NOW(), 0, 1),
('ECON-MICRO', '微观经济学基础', 'Khan Academy / CORE Econ', '经济管理', 2.00, 20, 'PUBLISHED',
 'Microeconomics and CORE Econ Open Resources', 'https://www.khanacademy.org/economics-finance-domain/microeconomics',
 '开放经济学学习资源，覆盖供需、市场均衡、弹性、消费者和生产者行为等基础主题。',
 180,
 '[{"title":"供需与均衡","description":"学习稀缺性、机会成本、供给需求和市场均衡。"},{"title":"弹性与福利","description":"理解价格弹性、消费者剩余、生产者剩余和效率。"},{"title":"市场结构入门","description":"学习成本、竞争、垄断和市场失灵基础。"}]',
 NOW(), NOW(), 0, 1);
