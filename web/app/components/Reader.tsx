import { forwardRef } from 'react'
import { assertUnreachable } from '@/app/lib/assert'
import {
	Node,
	Span,
	Table,
	TableCell,
	TableHeading,
} from '@/app/lib/types/usfm'
import { Passage } from '../lib/passage'

const styles = {
	h: 'text-2xl mb-4 mt-8 font-bold',
	p: 'mb-2 indent-4',
	pm: 'mb-2 ml-4',
	q: '-indent-4',
}

/* eslint-disable sort/object-properties */
const nodeStyles = {
	iex: 'text-base italic mb-2 text-zinc-600 dark:text-zinc-400',
	d: 'italic mb-4',
	sp: 'italic mt-4',
	p: styles.p,
	nb: styles.p,
	pm: `${styles.pm} indent-2`,
	pmo: styles.pm,
	pmc: styles.pm,
	pmr: `${styles.pm} text-right`,
	pc: 'mb-2 text-center',
	pr: 'mb-2 text-right',
	cls: 'mb-2 text-right',
	pi1: `${styles.p} ml-4`,
	pi2: `${styles.p} ml-8`,
	pi3: `${styles.p} ml-12`,
	q1: `${styles.q} pl-4`,
	q2: `${styles.q} pl-8`,
	q3: `${styles.q} pl-12`,
	q4: `${styles.q} pl-16`,
	qa: 'text-center italic',
	qr: 'text-right',
	qc: 'text-center',
	qm1: `${styles.q} pl-8`,
	qm2: `${styles.q} pl-12`,
	li1: 'ml-4',
	li2: 'ml-8',
	li3: 'ml-12',
	li4: 'ml-16',
	lim: 'ml-4',
	m: 'mb-2',
	mi: 'mb-2 ml-4',
} satisfies Partial<Record<Node[0], string>>
/* eslint-enable sort/object-properties */

type ReaderProps = {
	passage: Passage
}

export const Reader = forwardRef<HTMLDivElement, ReaderProps>(function Reader(
	{ passage },
	ref,
) {
	return (
		<div
			ref={ref}
			className="text-gray-900 dark:text-gray-200 font-sans text-lg"
			style={{
				viewTimeline: `--reader-${passage.id} block`,
			}}
		>
			{passage.nodes.map((node, index) => (
				<ReaderNode key={index} bookTitle={passage.bookTitle} node={node} />
			))}
		</div>
	)
})

function ReaderNode({ bookTitle, node }: { bookTitle: string; node: Node }) {
	const [type, children] = node

	switch (type) {
		case 'c': {
			return (
				<h2 className="mb-10 first:mt-0 mt-20 font-bold flex flex-col items-center">
					<span className="block text-lg text-zinc-600 dark:text-zinc-400 mb-2">
						{bookTitle}
					</span>
					<span className="block text-7xl">{children}</span>
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

function ReaderSpan({ node }: { node: Span }) {
	if (typeof node === 'string') {
		return <span>{node}</span>
	}

	const [type, value] = node
	switch (type) {
		case 'v':
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
		case 'add':
			return <span className="italic">{value}</span>

		case 'nd':
		case 'sc':
			return <span className="[font-variant:small-caps]">{value}</span>

		case 'sup':
			return <span className="align-super text-sm">{value}</span>

		// TODO
		case 'no':
			return <span>{value}</span>

		case 'qac':
			return <span className="font-bold italic">{value}</span>


		default:
			assertUnreachable(node)
	}
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

function renderChildren(nodes: Span[]) {
	return nodes.map((node, index) => <ReaderSpan key={index} node={node} />)
}
