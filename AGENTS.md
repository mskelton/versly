## Icons

Always use `painterResource` with drawable XML files for icons. Never use `Icons.*` from the Material Icons library.

```kotlin
// ❌ BAD
Icon(imageVector = Icons.Outlined.Delete, contentDescription = "...")

// ✅ GOOD
Icon(painter = painterResource(R.drawable.delete_24px), contentDescription = "...")
```

Drawable files live in `app/src/main/res/drawable/` and are named in the format `<name>_24px.xml`.
