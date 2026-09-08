const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const APP_PORT = 8080;

function listenerPid() {
  try {
    if (process.platform === 'win32') {
      const output = execSync(`netstat -ano | findstr :${APP_PORT}`, { stdio: ['pipe', 'pipe', 'ignore'] }).toString();
      const line = output.split('\n').find((item) => item.includes('LISTENING'));
      return line ? line.trim().split(/\s+/).at(-1) : '';
    }
    return execSync(`lsof -t -i:${APP_PORT}`, { stdio: ['pipe', 'pipe', 'ignore'] }).toString().trim();
  } catch {
    return '';
  }
}

const pid = listenerPid();
if (!pid) {
  console.log(`⚠️  Service not started: no listener on port ${APP_PORT}.`);
  console.log('   Start manually when needed: pwsh -ExecutionPolicy Bypass -File .\\init.ps1');
  console.log('   macOS/Linux/WSL/Git Bash: bash init.sh');
  process.exit(0);
}

console.log(`✓ Backend listening on port ${APP_PORT} (PID ${pid})`);
try {
  const branch = execSync('git branch --show-current', { stdio: ['pipe', 'pipe', 'ignore'] }).toString().trim();
  console.log(`Branch: ${branch || 'unknown'}`);
} catch {}

const planPath = path.join(process.cwd(), 'docs', '4-tasks', 'CURRENT_PLAN.md');
if (fs.existsSync(planPath)) {
  const active = fs.readFileSync(planPath, 'utf8').split('\n').find((line) => line.startsWith('## Active feature'));
  if (active) console.log(active);
}
