import { notFound } from 'next/navigation'
import { assertUnreachable } from '@/lib/assert'
import { db, sql } from '@/lib/db'
import {
	ChildNode,
	Node,
	Table,
	TableCell,
	TableHeading,
} from '@/lib/types/usfm'
import { TEST_PASSAGE } from './Reader.spec'

const styles = {
	h: 'text-2xl mb-4 mt-8 font-bold',
	p: 'mb-2 indent-4',
	pm: 'mb-2 indent-4 ml-4',
}

/* eslint-disable sort/object-properties */
const nodeStyles = {
	iex: 'text-base italic mb-2 text-zinc-600 dark:text-zinc-400',
	d: 'italic mb-4',
	sp: 'italic mt-4',
	p: styles.p,
	nb: styles.p,
	m: 'mb-2',
	pm: styles.pm,
	pmo: styles.pm,
	pmc: styles.pm,
	pmr: styles.pm,
	pc: 'text-center',
	pr: 'text-right',
	cls: 'text-right',
	pi1: `${styles.p} ml-4`,
	pi2: `${styles.p} ml-8`,
	pi3: `${styles.p} ml-12`,
	q1: 'ml-4',
	q2: 'ml-8',
	q3: 'ml-12',
	q4: 'ml-16',
	qa: 'text-center italic',
	qr: 'text-right',
	qc: 'text-center',
	li1: 'mb-2 ml-4',
	li2: 'mb-2 ml-8',
	li3: 'mb-2 ml-12',
	li4: 'mb-2 ml-16',
	mi: 'TODO',
	qm1: 'TODO',
	qm2: 'TODO',
	lim: 'TODO',
} satisfies Partial<Record<Node[0], string>>
/* eslint-enable sort/object-properties */

type ReaderProps = {
	passageRef: string
}

export async function Reader({ passageRef }: ReaderProps) {
	const passage = await getPassage(passageRef)

	return (
		<div className="text-gray-900 dark:text-gray-200 font-sans text-lg">
			{passage.map((node, index) => (
				<ReaderNode key={index} node={node} />
			))}
		</div>
	)
}

function ReaderNode({ node }: { node: Node }) {
	const [type, children] = node

	switch (type) {
		case 'cl': {
			return (
				<h2 className="mb-10 first:mt-0 mt-20 font-bold flex flex-col items-center">
					<span className="block text-lg text-zinc-600 dark:text-zinc-400 mb-2">
						{children}
					</span>
					<span className="block text-7xl">{node[2]}</span>
				</h2>
			)
		}

		case 'ms':
		case 's1':
			return <h3 className={styles.h}>{children}</h3>

		case 's2':
			return <h4 className={styles.h}>{children}</h4>

		case 's3':
			return <h5 className={styles.h}>{children}</h5>

		case 'table':
			return <ReaderTable node={node} />

		case 'b':
			return <div className="h-4" />

		default:
			return (
				<p className={`${type} leading-loose ${nodeStyles[type]}`}>
					{typeof children === 'string' ? children : renderChildren(children)}
				</p>
			)
	}
}

function ReaderChildNode({ node }: { node: ChildNode }) {
	const [type, value] = node

	switch (type) {
		case 'v':
			if (process.env.NODE_ENV === 'development') {
				return null
			}

			return (
				<span className="text-gray-500 relative align-super -top-0.5 text-xs">
					{value}&nbsp;
				</span>
			)

		case 'qs':
			return <span className="italic float-right">{value}</span>

		case 'litl':
			return <span className="float-right">{value}</span>

		case 'wj':
			return <span className="text-red-600 dark:text-red-500">{value}</span>

		case 'em':
		case 'bd':
			return <span className="font-bold">{value}</span>

		case 'bk':
		case 'qt':
		case 'sig':
		case 'sls':
		case 'tl':
		case 'it':
			return <span className="italic">{value}</span>

		case 'nd':
		case 'sc':
			return <span className="[font-variant:small-caps]">{value}</span>

		case 'sup':
			return <span className="align-super text-sm">{value}</span>

		case 't':
			return <span>{value}</span>

		default:
			assertUnreachable(node)
	}
}

function renderChildren(nodes: ChildNode[]) {
	return nodes.map((node, index) => <ReaderChildNode key={index} node={node} />)
}

function ReaderTable({ node }: { node: Table }) {
	const { bodyRows, headerRows } = node[1].reduce(
		(acc, row) => {
			if (row.every((cell) => cell[0] === 'th')) {
				acc.headerRows.push(row as TableHeading[])
			} else {
				acc.bodyRows.push(row as TableCell[])
			}

			return acc
		},
		{
			bodyRows: [] as TableCell[][],
			headerRows: [] as TableHeading[][],
		},
	)

	return (
		<table>
			{headerRows ? (
				<thead>
					{headerRows.map((row, index) => (
						<tr key={index}>
							{row.map((cell, index) => (
								<th key={index}>{renderChildren(cell[1])}</th>
							))}
						</tr>
					))}
				</thead>
			) : null}

			<tbody>
				{bodyRows.map((row, index) => (
					<tr key={index}>
						{row.map((cell, index) => (
							<td key={index}>{renderChildren(cell[1])}</td>
						))}
					</tr>
				))}
			</tbody>
		</table>
	)
}

const getPassageQuery = db.prepare<
	{ ref: string },
	{ book: string; chapter: string; data: string }
>(
	sql`
    SELECT book.title as book, chapter.ref as chapter, chapter.data
    FROM chapter
    JOIN book ON book.ref = chapter.book_ref
    WHERE chapter.ref = @ref
  `,
)

async function getPassage(ref: string): Promise<Node[]> {
	if (process.env.NODE_ENV === 'development') {
		return TEST_PASSAGE
	}

	const row = getPassageQuery.get({ ref })
	if (!row) {
		notFound()
	}

	const chapterNumber = row.chapter.split('.')[1]

	return [['cl', row.book, chapterNumber], ...JSON.parse(row.data)]
}
