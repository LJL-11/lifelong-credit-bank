const DISPLAY_LABELS = {
  ACTIVE: "启用",
  DISABLED: "禁用",
  OPEN: "开放",
  CLOSED: "关闭",
  UPCOMING: "未开始",
  ENDED: "已结束",
  PUBLISHED: "已发布",
  OFFLINE: "已下线",
  VISIBLE: "可见",
  HIDDEN: "隐藏",
  APPROVED: "已通过",
  REJECTED: "已驳回",
  PENDING: "待处理",
  SUBMITTED: "已提交",
  VIEWED: "已查看",
  ACCEPTED: "已接受",
  PASSED: "已通过",
  FAILED: "未通过",
  LEARNING: "学习中",
  INCREASE: "增加",
  CONSUME: "消费",
  PAID: "已支付",
  DELIVERED: "已发货",
  CANCELLED: "已取消",
  REFUNDED: "已退款",
  COURSE: "课程",
  MERCHANDISE: "实物",
  CERTIFICATE: "认证",
  SERVICE: "服务",
  ADMIN_GRANT: "管理员发放",
  ORDER_PAY: "订单支付",
  COURSE_COMPLETE: "课程完成",
  DAILY_SIGNIN: "每日签到",
  ACHIEVEMENT_APPROVED: "成果认定",
  MANUAL: "手动处理",
};

export function displayLabel(value) {
  if (value === null || value === undefined || value === "") return "-";
  const key = String(value).trim();
  return DISPLAY_LABELS[key] || key;
}
