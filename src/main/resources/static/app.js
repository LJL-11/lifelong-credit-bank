const modules = {
    learners: {
        title: "学员档案",
        subtitle: "维护终身学习用户的基础信息与状态",
        api: "/api/admin/learners",
        columns: ["id", "username", "realName", "phone", "educationLevel", "status"]
    },
    courses: {
        title: "课程资源",
        subtitle: "管理可兑换学分和积分的课程资源",
        api: "/api/admin/courses",
        columns: ["id", "courseCode", "courseName", "provider", "category", "creditPoint", "status"]
    },
    "credit-accounts": {
        title: "积分账户",
        subtitle: "查看学员积分余额、累计积分和冻结积分",
        api: "/api/admin/credit-accounts",
        columns: ["id", "learnerId", "totalCredits", "availableCredits", "frozenCredits", "accountStatus"]
    },
    achievements: {
        title: "成果认定",
        subtitle: "管理学习成果、证书、竞赛和实践成果审核",
        api: "/api/admin/achievements",
        columns: ["id", "learnerId", "achievementName", "achievementType", "suggestedCredits", "auditStatus"]
    },
    "forum-posts": {
        title: "论坛管理",
        subtitle: "维护学习交流区内容和可见状态",
        api: "/api/admin/forum-posts",
        columns: ["id", "learnerId", "title", "section", "replyCount", "status"]
    },
    jobs: {
        title: "招聘求职",
        subtitle: "发布和维护面向学习者的招聘职位",
        api: "/api/admin/jobs",
        columns: ["id", "companyName", "positionName", "city", "contact", "status"]
    }
};

const title = document.querySelector("#page-title");
const subtitle = document.querySelector("#page-subtitle");
const dashboard = document.querySelector("#dashboard");
const tablePanel = document.querySelector("#table-panel");
const tableTitle = document.querySelector("#table-title");
const tableHead = document.querySelector("#table-head");
const tableBody = document.querySelector("#table-body");
let activeModule = "dashboard";

document.querySelectorAll("nav button").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll("nav button").forEach((item) => item.classList.remove("active"));
        button.classList.add("active");
        activeModule = button.dataset.module;
        render();
    });
});

document.querySelector("#refresh-btn").addEventListener("click", render);

async function render() {
    if (activeModule === "dashboard") {
        await renderDashboard();
        return;
    }
    await renderTable(modules[activeModule]);
}

async function renderDashboard() {
    title.textContent = "平台总览";
    subtitle.textContent = "学习、成果、积分、社区与就业数据统一管理";
    dashboard.classList.remove("hidden");
    tablePanel.classList.add("hidden");

    const response = await fetch("/api/admin/dashboard/stats");
    const result = await response.json();
    const stats = result.data || {};
    const cards = [
        ["学员总数", stats.learnerCount],
        ["课程资源", stats.courseCount],
        ["成果认定", stats.achievementCount],
        ["积分流水", stats.creditTransactionCount],
        ["论坛帖子", stats.forumPostCount],
        ["招聘岗位", stats.jobPostingCount]
    ];
    dashboard.innerHTML = `<div class="stats-grid">${cards.map(([label, value]) => `
        <article class="stat">
            <span>${label}</span>
            <strong>${value ?? 0}</strong>
        </article>
    `).join("")}</div>`;
}

async function renderTable(module) {
    title.textContent = module.title;
    subtitle.textContent = module.subtitle;
    tableTitle.textContent = `${module.title}列表`;
    dashboard.classList.add("hidden");
    tablePanel.classList.remove("hidden");

    const response = await fetch(module.api);
    const result = await response.json();
    const rows = result.data?.records || [];
    tableHead.innerHTML = `<tr>${module.columns.map((column) => `<th>${column}</th>`).join("")}</tr>`;
    tableBody.innerHTML = rows.map((row) => `
        <tr>${module.columns.map((column) => `<td>${row[column] ?? ""}</td>`).join("")}</tr>
    `).join("");
}

render();
