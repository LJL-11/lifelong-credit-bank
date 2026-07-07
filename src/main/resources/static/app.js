const { createApp } = Vue;

const modules = {
    dashboard: {
        title: "平台总览",
        subtitle: "统一查看学员、课程、成果认定、积分流水与就业信息",
    },
    learners: {
        title: "学员档案",
        subtitle: "维护终身学习用户的基础信息与账号状态",
        tableTitle: "学员列表",
        api: "/api/admin/learners",
        canCreate: true,
        canEdit: true,
        canDelete: true,
        columns: [
            { key: "id", label: "ID" },
            { key: "username", label: "用户名" },
            { key: "realName", label: "姓名" },
            { key: "phone", label: "手机号" },
            { key: "email", label: "邮箱" },
            { key: "educationLevel", label: "学历" },
            { key: "status", label: "状态", badge: true },
        ],
        fields: [
            { key: "username", label: "用户名" },
            { key: "realName", label: "姓名" },
            { key: "phone", label: "手机号" },
            { key: "email", label: "邮箱" },
            { key: "educationLevel", label: "学历" },
            { key: "status", label: "状态", placeholder: "ACTIVE / DISABLED" },
        ],
    },
    courses: {
        title: "课程资源",
        subtitle: "管理课程编码、提供方、学分与积分信息",
        tableTitle: "课程列表",
        api: "/api/admin/courses",
        canCreate: true,
        canEdit: true,
        canDelete: true,
        columns: [
            { key: "id", label: "ID" },
            { key: "courseCode", label: "课程编码" },
            { key: "courseName", label: "课程名称" },
            { key: "provider", label: "提供方" },
            { key: "category", label: "分类" },
            { key: "creditValue", label: "学分" },
            { key: "creditPoint", label: "积分" },
            { key: "status", label: "状态", badge: true },
        ],
        fields: [
            { key: "courseCode", label: "课程编码" },
            { key: "courseName", label: "课程名称" },
            { key: "provider", label: "提供方" },
            { key: "category", label: "分类" },
            { key: "creditValue", label: "学分", type: "number" },
            { key: "creditPoint", label: "积分", type: "number" },
            { key: "status", label: "状态", placeholder: "ONLINE / OFFLINE" },
        ],
    },
    "credit-accounts": {
        title: "积分账户",
        subtitle: "查看账户余额，执行开户、增加积分和消费积分操作",
        tableTitle: "积分账户列表",
        api: "/api/admin/credit-accounts",
        operations: { credit: true, openAccount: true },
        columns: [
            { key: "id", label: "ID" },
            { key: "learnerId", label: "学员 ID" },
            { key: "totalCredits", label: "累计积分" },
            { key: "availableCredits", label: "可用积分" },
            { key: "frozenCredits", label: "冻结积分" },
            { key: "accountStatus", label: "账户状态", badge: true },
        ],
        fields: [],
    },
    "credit-transactions": {
        title: "积分流水",
        subtitle: "追踪积分增加、消费和余额变化记录",
        tableTitle: "积分流水列表",
        api: "/api/admin/credit-transactions",
        columns: [
            { key: "id", label: "ID" },
            { key: "learnerId", label: "学员 ID" },
            { key: "accountId", label: "账户 ID" },
            { key: "transactionType", label: "类型", badge: true },
            { key: "amount", label: "数量" },
            { key: "balanceAfter", label: "变更后余额" },
            { key: "sourceType", label: "来源" },
            { key: "remark", label: "备注" },
        ],
        fields: [],
    },
    achievements: {
        title: "成果认定",
        subtitle: "管理学习成果、证书、竞赛和实践成果审核",
        tableTitle: "成果列表",
        api: "/api/admin/achievements",
        canCreate: true,
        canEdit: true,
        columns: [
            { key: "id", label: "ID" },
            { key: "learnerId", label: "学员 ID" },
            { key: "achievementName", label: "成果名称" },
            { key: "achievementType", label: "类型" },
            { key: "suggestedCredits", label: "建议积分" },
            { key: "auditStatus", label: "审核状态", badge: true },
            { key: "auditor", label: "审核人" },
        ],
        fields: [
            { key: "learnerId", label: "学员 ID", type: "number" },
            { key: "achievementName", label: "成果名称" },
            { key: "achievementType", label: "成果类型" },
            { key: "suggestedCredits", label: "建议积分", type: "number" },
            { key: "auditStatus", label: "审核状态", placeholder: "PENDING / APPROVED / REJECTED" },
            { key: "auditor", label: "审核人" },
        ],
    },
    "learning-records": {
        title: "学习记录",
        subtitle: "记录课程学习进度、成绩和完成状态",
        tableTitle: "学习记录列表",
        api: "/api/admin/learning-records",
        canCreate: true,
        canEdit: true,
        columns: [
            { key: "id", label: "ID" },
            { key: "learnerId", label: "学员 ID" },
            { key: "courseId", label: "课程 ID" },
            { key: "progress", label: "进度" },
            { key: "score", label: "成绩" },
            { key: "result", label: "结果", badge: true },
            { key: "completedAt", label: "完成时间" },
        ],
        fields: [
            { key: "learnerId", label: "学员 ID", type: "number" },
            { key: "courseId", label: "课程 ID", type: "number" },
            { key: "progress", label: "进度", type: "number" },
            { key: "score", label: "成绩", type: "number" },
            { key: "result", label: "结果", placeholder: "PASSED / FAILED / STUDYING" },
        ],
    },
    "forum-posts": {
        title: "论坛管理",
        subtitle: "维护学习交流社区内容、板块和可见状态",
        tableTitle: "帖子列表",
        api: "/api/admin/forum-posts",
        canCreate: true,
        columns: [
            { key: "id", label: "ID" },
            { key: "learnerId", label: "学员 ID" },
            { key: "title", label: "标题" },
            { key: "section", label: "板块" },
            { key: "replyCount", label: "回复数" },
            { key: "status", label: "状态", badge: true },
        ],
        fields: [
            { key: "learnerId", label: "学员 ID", type: "number" },
            { key: "title", label: "标题" },
            { key: "section", label: "板块" },
            { key: "content", label: "内容", type: "textarea" },
            { key: "replyCount", label: "回复数", type: "number" },
            { key: "status", label: "状态", placeholder: "VISIBLE / HIDDEN" },
        ],
    },
    jobs: {
        title: "招聘求职",
        subtitle: "发布和维护面向学习者的招聘岗位",
        tableTitle: "岗位列表",
        api: "/api/admin/jobs",
        canCreate: true,
        columns: [
            { key: "id", label: "ID" },
            { key: "companyName", label: "公司" },
            { key: "positionName", label: "岗位" },
            { key: "city", label: "城市" },
            { key: "contact", label: "联系方式" },
            { key: "status", label: "状态", badge: true },
        ],
        fields: [
            { key: "companyName", label: "公司" },
            { key: "positionName", label: "岗位" },
            { key: "city", label: "城市" },
            { key: "requirement", label: "要求", type: "textarea" },
            { key: "contact", label: "联系方式" },
            { key: "status", label: "状态", placeholder: "OPEN / CLOSED" },
        ],
    },
};

createApp({
    data() {
        return {
            activeModule: "dashboard",
            stats: {},
            rows: [],
            loading: false,
            saving: false,
            error: "",
            apiOnline: false,
            page: {
                current: 1,
                size: 10,
                total: 0,
            },
            editor: {
                open: false,
                row: null,
                form: {},
            },
            creditDialog: {
                open: false,
                type: "increase",
                form: {},
            },
            accountDialog: {
                open: false,
                learnerId: "",
            },
            toast: {
                text: "",
                type: "ok",
            },
        };
    },
    computed: {
        menu() {
            return [
                { key: "dashboard", label: "平台总览", icon: "01" },
                { key: "learners", label: "学员档案", icon: "02" },
                { key: "courses", label: "课程资源", icon: "03" },
                { key: "credit-accounts", label: "积分账户", icon: "04" },
                { key: "credit-transactions", label: "积分流水", icon: "05" },
                { key: "achievements", label: "成果认定", icon: "06" },
                { key: "learning-records", label: "学习记录", icon: "07" },
                { key: "forum-posts", label: "论坛管理", icon: "08" },
                { key: "jobs", label: "招聘求职", icon: "09" },
            ];
        },
        currentModule() {
            return modules[this.activeModule];
        },
        totalPages() {
            return Math.max(1, Math.ceil(this.page.total / this.page.size));
        },
        statCards() {
            return [
                { key: "learnerCount", label: "学员总数", value: this.stats.learnerCount ?? 0, note: "已建档学员" },
                { key: "courseCount", label: "课程资源", value: this.stats.courseCount ?? 0, note: "可认定课程" },
                { key: "achievementCount", label: "成果认定", value: this.stats.achievementCount ?? 0, note: "审核记录" },
                { key: "creditTransactionCount", label: "积分流水", value: this.stats.creditTransactionCount ?? 0, note: "积分变动" },
                { key: "forumPostCount", label: "论坛帖子", value: this.stats.forumPostCount ?? 0, note: "社区内容" },
                { key: "jobPostingCount", label: "招聘岗位", value: this.stats.jobPostingCount ?? 0, note: "就业机会" },
            ];
        },
        overview() {
            return [
                { label: "当前模块", value: this.currentModule.title },
                { label: "分页大小", value: `${this.page.size} 条/页` },
                { label: "接口状态", value: this.apiOnline ? "正常" : "待检查" },
            ];
        },
        quickActions() {
            return this.menu.filter((item) => item.key !== "dashboard").slice(0, 6);
        },
    },
    mounted() {
        this.loadDashboard();
    },
    methods: {
        async request(url, options = {}) {
            const response = await fetch(url, {
                headers: { "Content-Type": "application/json", ...(options.headers || {}) },
                ...options,
            });
            const result = await response.json();
            if (!response.ok || result.code !== 200) {
                throw new Error(result.message || "请求失败");
            }
            return result.data;
        },
        async loadDashboard() {
            this.loading = true;
            this.error = "";
            try {
                this.stats = await this.request("/api/admin/dashboard/stats");
                this.apiOnline = true;
            } catch (error) {
                this.apiOnline = false;
                this.error = error.message;
                this.showToast(error.message, "error");
            } finally {
                this.loading = false;
            }
        },
        async loadTable() {
            this.loading = true;
            this.error = "";
            try {
                const data = await this.request(`${this.currentModule.api}?current=${this.page.current}&size=${this.page.size}`);
                this.rows = data.records || [];
                this.page.total = data.total || 0;
                this.page.current = data.current || this.page.current;
                this.apiOnline = true;
            } catch (error) {
                this.rows = [];
                this.apiOnline = false;
                this.error = error.message;
            } finally {
                this.loading = false;
            }
        },
        switchModule(key) {
            this.activeModule = key;
            this.page.current = 1;
            this.rows = [];
            this.error = "";
            if (key === "dashboard") {
                this.loadDashboard();
            } else {
                this.loadTable();
            }
        },
        reloadActive() {
            if (this.activeModule === "dashboard") {
                this.loadDashboard();
            } else {
                this.loadTable();
            }
        },
        changePage(nextPage) {
            this.page.current = Math.min(Math.max(nextPage, 1), this.totalPages);
            this.loadTable();
        },
        openEditor(row = null) {
            const form = {};
            this.currentModule.fields.forEach((field) => {
                form[field.key] = row?.[field.key] ?? "";
            });
            this.editor = { open: true, row, form };
        },
        closeEditor() {
            this.editor = { open: false, row: null, form: {} };
        },
        normalizePayload(form, fields = []) {
            const numericKeys = new Set(fields.filter((field) => field.type === "number").map((field) => field.key));
            return Object.entries(form).reduce((payload, [key, value]) => {
                if (value === "") {
                    return payload;
                }
                payload[key] = numericKeys.has(key) ? Number(value) : value;
                return payload;
            }, {});
        },
        async submitEditor() {
            const payload = this.normalizePayload(this.editor.form, this.currentModule.fields);
            const id = this.editor.row?.id;
            const method = id ? "PUT" : "POST";
            const url = id ? `${this.currentModule.api}/${id}` : this.currentModule.api;
            this.saving = true;
            try {
                await this.request(url, { method, body: JSON.stringify(payload) });
                this.showToast("保存成功");
                this.closeEditor();
                await this.loadTable();
            } catch (error) {
                this.showToast(error.message, "error");
            } finally {
                this.saving = false;
            }
        },
        async removeRow(row) {
            if (!window.confirm(`确认删除 ID ${row.id}？`)) {
                return;
            }
            try {
                await this.request(`${this.currentModule.api}/${row.id}`, { method: "DELETE" });
                this.showToast("删除成功");
                await this.loadTable();
            } catch (error) {
                this.showToast(error.message, "error");
            }
        },
        openCreditDialog(type) {
            this.creditDialog = {
                open: true,
                type,
                form: { learnerId: "", amount: "", sourceType: "MANUAL", sourceNo: "", remark: "" },
            };
        },
        closeCreditDialog() {
            this.creditDialog.open = false;
        },
        async submitCreditDialog() {
            const payload = this.normalizePayload(this.creditDialog.form, [
                { key: "learnerId", type: "number" },
                { key: "amount", type: "number" },
            ]);
            const url = `/api/admin/credit-accounts/${this.creditDialog.type}`;
            this.saving = true;
            try {
                await this.request(url, { method: "POST", body: JSON.stringify(payload) });
                this.showToast("操作成功");
                this.closeCreditDialog();
                await this.loadTable();
            } catch (error) {
                this.showToast(error.message, "error");
            } finally {
                this.saving = false;
            }
        },
        openAccountDialog() {
            this.accountDialog = { open: true, learnerId: "" };
        },
        closeAccountDialog() {
            this.accountDialog.open = false;
        },
        async submitOpenAccount() {
            this.saving = true;
            try {
                await this.request(`/api/admin/credit-accounts/open/${this.accountDialog.learnerId}`, { method: "POST" });
                this.showToast("账户已开通");
                this.closeAccountDialog();
                await this.loadTable();
            } catch (error) {
                this.showToast(error.message, "error");
            } finally {
                this.saving = false;
            }
        },
        formatCell(value) {
            if (value === null || value === undefined || value === "") {
                return "-";
            }
            if (typeof value === "string" && value.includes("T")) {
                return value.replace("T", " ").slice(0, 16);
            }
            return value;
        },
        badgeClass(value) {
            const text = String(value || "").toLowerCase();
            if (text.includes("active") || text.includes("open") || text.includes("online") || text.includes("approved") || text.includes("passed") || text.includes("increase")) {
                return "success";
            }
            if (text.includes("pending") || text.includes("studying")) {
                return "pending";
            }
            if (text.includes("reject") || text.includes("disable") || text.includes("closed") || text.includes("failed") || text.includes("consume")) {
                return "danger";
            }
            return "neutral";
        },
        showToast(text, type = "ok") {
            this.toast = { text, type };
            window.clearTimeout(this.toastTimer);
            this.toastTimer = window.setTimeout(() => {
                this.toast.text = "";
            }, 2600);
        },
    },
}).mount("#app");
