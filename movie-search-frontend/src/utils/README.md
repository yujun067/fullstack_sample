# Utils Directory

This directory contains utility functions and higher-order components (HOCs) that are used across the application.

## Structure

```
src/utils/
├── withMaintenanceCheck.tsx    # HOC for maintenance mode handling
├── withAuth.tsx                # HOC for authentication (future)
├── withErrorBoundary.tsx       # HOC for error boundaries (future)
└── README.md                   # This file
```

## Usage

### HOCs (Higher-Order Components)
```tsx
import { withMaintenanceCheck } from '../utils/withMaintenanceCheck';

const MyPage = withMaintenanceCheck(MyPageComponent);
export default MyPage;
```

### Why utils/ instead of components/hoc/?
- HOCs are utility functions, not UI components
- Follows React ecosystem conventions
- Keeps components/ folder focused on actual UI components
- Easier to find and maintain utility code

## Best Practices
- Keep HOCs focused on a single concern
- Use descriptive names with `with` prefix
- Export both named and default exports
- Include proper TypeScript types
