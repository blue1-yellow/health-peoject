// Moderate walking: 3.5 MET total, or 2.5 MET above resting expenditure.
export function activityGoal(age, weight, calories) {
  const years = Number(age);
  const kilograms = Number(weight);
  if (!Number.isInteger(years) || years < 18 || years > 120 || !Number.isFinite(kilograms) || kilograms < 10 || kilograms > 350) return null;
  const target = Math.round(2.5 * kilograms * 150 / (60 * 7));
  if (calories === '' || calories == null || !Number.isFinite(Number(calories)) || Number(calories) < 0) return { target };
  const remaining = Math.max(0, target - Number(calories));
  return { target, remaining, steps: Math.ceil(remaining / (2.5 * kilograms / 6000)) };
}
