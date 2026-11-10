import { test } from 'node:test';
import assert from 'node:assert/strict';
import { activityGoal } from './activityGoal.js';

test('estimates an adult goal and remaining walking steps', () => {
  assert.deepEqual(activityGoal(30, 70, 40), { target: 63, remaining: 23, steps: 789 });
  assert.deepEqual(activityGoal(65, 70, 100), { target: 63, remaining: 0, steps: 0 });
});

test('requires adult age, weight and a confirmed calorie value', () => {
  assert.equal(activityGoal(17, 70, 40), null);
  assert.equal(activityGoal(30, null, 40), null);
  assert.deepEqual(activityGoal(30, 70, ''), { target: 63 });
  assert.deepEqual(activityGoal(30, 70, -1), { target: 63 });
});
